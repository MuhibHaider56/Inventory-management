package com.bachat.inventory.service;

import com.bachat.inventory.domain.*;
import com.bachat.inventory.dto.*;
import com.bachat.inventory.exception.BadRequestException;
import com.bachat.inventory.exception.ResourceNotFoundException;
import com.bachat.inventory.repository.*;
import com.bachat.inventory.util.MoneyUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final SalesOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final ExpenseRepository expenseRepository; // NEW

    public OrderService(SalesOrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        InventoryRepository inventoryRepository,
                        OrderPaymentRepository orderPaymentRepository,
                        ExpenseRepository expenseRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderPaymentRepository = orderPaymentRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public OrderResponse placeOrder(OrderCreateRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BadRequestException("Order items must not be empty");
        }

        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + req.getCustomerId()));

        SalesOrder order = new SalesOrder();
        order.setCustomer(customer);
        order.setStatus(req.getStatus() == null ? OrderStatus.COMPLETED : req.getStatus());

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : req.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Item quantity must be > 0");
            }

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: id=" + itemReq.getProductId()));

            BigDecimal qty = MoneyUtil.scale2(itemReq.getQuantity());

            Inventory inv = inventoryRepository.findByProductIdForUpdate(product.getId())
                    .orElseThrow(() -> new BadRequestException("No inventory record for productId=" + product.getId()));

            if (inv.getQuantityAvailable().compareTo(qty) < 0) {
                throw new BadRequestException("Not enough stock for product '" + product.getName() +
                        "'. Available=" + inv.getQuantityAvailable() + " " + product.getUnit() +
                        ", requested=" + qty + " " + product.getUnit());
            }

            inv.setQuantityAvailable(MoneyUtil.subtract(inv.getQuantityAvailable(), qty));
            inventoryRepository.save(inv);

            BigDecimal sellingPrice = itemReq.getSellingPrice() != null
                    ? MoneyUtil.scale2(itemReq.getSellingPrice())
                    : MoneyUtil.scale2(product.getSellingPrice());

            BigDecimal costPrice = MoneyUtil.scale2(product.getCostPrice());

            if (sellingPrice.compareTo(costPrice) < 0) {
                throw new BadRequestException("Selling price cannot be below cost price for product: " + product.getName());
            }

            BigDecimal lineTotal = MoneyUtil.multiply(sellingPrice, qty);
            BigDecimal lineProfit = MoneyUtil.multiply(MoneyUtil.subtract(sellingPrice, costPrice), qty);

            OrderItem oi = new OrderItem();
            oi.setProduct(product);
            oi.setQuantity(qty);
            oi.setSellingPrice(sellingPrice);
            oi.setCostPrice(costPrice);
            oi.setTotalPrice(lineTotal);
            oi.setProfit(lineProfit);

            order.addItem(oi);

            totalAmount = MoneyUtil.add(totalAmount, lineTotal);
            totalProfit = MoneyUtil.add(totalProfit, lineProfit);
        }

        // NEW: Process expenses at order creation
        BigDecimal totalExpenses = BigDecimal.ZERO;
        if (req.getExpenses() != null && !req.getExpenses().isEmpty()) {
            for (ExpenseCreateRequest expReq : req.getExpenses()) {
                totalExpenses = MoneyUtil.add(totalExpenses, MoneyUtil.scale2(expReq.getAmount()));
            }
        }

        // Deduct expenses from profit
        order.setTotalAmount(MoneyUtil.scale2(totalAmount));
        order.setTotalExpenses(MoneyUtil.scale2(totalExpenses));
        order.setTotalProfit(MoneyUtil.subtract(MoneyUtil.scale2(totalProfit), MoneyUtil.scale2(totalExpenses)));

        // Handle amountPaid + paymentDueDate
        BigDecimal amountPaid = req.getAmountPaid() != null
                ? MoneyUtil.scale2(req.getAmountPaid())
                : BigDecimal.ZERO;

        if (amountPaid.compareTo(MoneyUtil.scale2(totalAmount)) > 0) {
            throw new BadRequestException("amountPaid cannot exceed totalAmount=" + MoneyUtil.scale2(totalAmount));
        }

        if (amountPaid.compareTo(BigDecimal.ZERO) > 0
                && amountPaid.compareTo(MoneyUtil.scale2(totalAmount)) < 0
                && req.getPaymentDueDate() == null) {
            throw new BadRequestException("paymentDueDate is required when amountPaid is a partial payment");
        }

        order.setAmountPaid(amountPaid);
        order.setPaymentDueDate(req.getPaymentDueDate());

        // Save order first so Expense and OrderPayment can reference it
        SalesOrder saved = orderRepository.save(order);

        // NEW: Save expense records linked to this order
        if (req.getExpenses() != null && !req.getExpenses().isEmpty()) {
            for (ExpenseCreateRequest expReq : req.getExpenses()) {
                Expense expense = new Expense();
                expense.setOrder(saved);
                expense.setTitle(expReq.getTitle());
                expense.setDescription(expReq.getDescription());
                expense.setAmount(MoneyUtil.scale2(expReq.getAmount()));
                expense.setExpenseDate(expReq.getExpenseDate() != null
                        ? expReq.getExpenseDate()
                        : saved.getOrderDate().toLocalDate());
                expenseRepository.save(expense);
            }
        }

        // Create OrderPayment record if any amount was paid
        if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            OrderPayment payment = new OrderPayment();
            payment.setOrder(saved);
            payment.setAmount(amountPaid);
            payment.setMethod(req.getPaymentMethod());
            payment.setReference(req.getPaymentReference());
            payment.setNote(req.getPaymentNote() != null
                    ? req.getPaymentNote()
                    : "Initial payment at order creation");
            orderPaymentRepository.save(payment);
        }

        saved.setPaymentStatus(calcPaymentStatus(saved));
        saved = orderRepository.save(saved);

        return toResponseDetailed(saved);
    }

    // NEW: Add expense to an existing order
    @Transactional
    public OrderResponse addExpenseToOrder(Long orderId, ExpenseCreateRequest req) {
        SalesOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: id=" + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot add expense to a CANCELLED order");
        }

        BigDecimal amount = MoneyUtil.scale2(req.getAmount());

        Expense expense = new Expense();
        expense.setOrder(order);
        expense.setTitle(req.getTitle());
        expense.setDescription(req.getDescription());
        expense.setAmount(amount);
        expense.setExpenseDate(req.getExpenseDate() != null
                ? req.getExpenseDate()
                : order.getOrderDate().toLocalDate());
        expenseRepository.save(expense);

        // Recalculate totalExpenses and totalProfit on the order
        order.setTotalExpenses(MoneyUtil.add(order.getTotalExpenses(), amount));
        order.setTotalProfit(MoneyUtil.subtract(order.getTotalProfit(), amount));
        orderRepository.save(order);

        return toResponseDetailed(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        SalesOrder order = orderRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: id=" + id));
        return toResponseDetailed(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> list(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponseSummary);
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        SalesOrder order = orderRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: id=" + id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }

        for (OrderItem item : order.getItems()) {
            Long productId = item.getProduct().getId();
            Inventory inv = inventoryRepository.findByProductIdForUpdate(productId)
                    .orElseThrow(() -> new BadRequestException("Inventory missing for productId=" + productId));
            inv.setQuantityAvailable(MoneyUtil.add(inv.getQuantityAvailable(), item.getQuantity()));
            inventoryRepository.save(inv);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return toResponseDetailed(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long orderId) {
        SalesOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        InvoiceResponse r = new InvoiceResponse();
        r.setOrderId(order.getId());
        r.setCustomerName(order.getCustomer().getName());
        r.setOrderDate(order.getOrderDate());
        r.setPaymentDueDate(order.getPaymentDueDate());
        r.setPaymentStatus(order.getPaymentStatus().name());
        r.setTotalAmount(order.getTotalAmount());
        r.setAmountPaid(order.getAmountPaid());
        r.setBalanceDue(order.getTotalAmount().subtract(order.getAmountPaid()));
        r.setTotalExpenses(order.getTotalExpenses()); // NEW

        // Line items
        r.setItems(order.getItems().stream().map(oi -> {
            InvoiceItem ii = new InvoiceItem();
            ii.setProduct(oi.getProduct().getName());
            ii.setQuantity(oi.getQuantity());
            ii.setUnit(oi.getProduct().getUnit());
            ii.setSellingPrice(oi.getSellingPrice());
            ii.setTotalPrice(oi.getTotalPrice());
            return ii;
        }).toList());

        // Payments
        r.setPayments(orderPaymentRepository
                .findByOrderIdOrderByPaymentDateAsc(orderId).stream()
                .map(p -> {
                    InvoicePayment ip = new InvoicePayment();
                    ip.setAmount(p.getAmount());
                    ip.setPaymentDate(p.getPaymentDate());
                    ip.setMethod(p.getMethod());
                    ip.setReference(p.getReference());
                    return ip;
                }).toList());

        // NEW: Expenses linked to this order
        r.setExpenses(expenseRepository
                .findByOrderIdOrderByExpenseDateAsc(orderId).stream()
                .map(e -> {
                    InvoiceExpense ie = new InvoiceExpense();
                    ie.setTitle(e.getTitle());
                    ie.setDescription(e.getDescription());
                    ie.setAmount(e.getAmount());
                    ie.setExpenseDate(e.getExpenseDate());
                    return ie;
                }).toList());

        return r;
    }

    @Transactional
    public OrderResponse addPayment(Long orderId, PaymentCreateRequest req) {
        SalesOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: id=" + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot add payment to a CANCELLED order");
        }

        BigDecimal amount = MoneyUtil.scale2(req.getAmount());
        BigDecimal remaining = MoneyUtil.subtract(order.getTotalAmount(), order.getAmountPaid());

        if (amount.compareTo(remaining) > 0) {
            throw new BadRequestException("Payment exceeds remaining balance. Remaining=" + remaining);
        }

        OrderPayment p = new OrderPayment();
        p.setOrder(order);
        p.setAmount(amount);
        p.setMethod(req.getMethod());
        p.setReference(req.getReference());
        p.setNote(req.getNote());
        orderPaymentRepository.save(p);

        order.setAmountPaid(MoneyUtil.add(order.getAmountPaid(), amount));
        order.setPaymentStatus(calcPaymentStatus(order));
        orderRepository.save(order);

        return toResponseDetailed(order);
    }

    private PaymentStatus calcPaymentStatus(SalesOrder o) {
        if (o.getAmountPaid().compareTo(BigDecimal.ZERO) == 0) return PaymentStatus.UNPAID;
        if (o.getAmountPaid().compareTo(o.getTotalAmount()) < 0) return PaymentStatus.PARTIALLY_PAID;
        return PaymentStatus.PAID;
    }

    private OrderResponse toResponseSummary(SalesOrder order) {
        OrderResponse res = new OrderResponse();
        res.setId(order.getId());
        res.setCustomerId(order.getCustomer() != null ? order.getCustomer().getId() : null);
        res.setCustomerName(order.getCustomer() != null ? order.getCustomer().getName() : null);
        res.setCustomerPhone(order.getCustomer() != null ? order.getCustomer().getPhone() : null);
        res.setCustomerAddress(order.getCustomer() != null ? order.getCustomer().getAddress() : null);
        res.setOrderDate(order.getOrderDate());
        res.setStatus(order.getStatus());
        res.setTotalAmount(order.getTotalAmount());
        res.setTotalProfit(order.getTotalProfit());
        res.setTotalExpenses(order.getTotalExpenses()); // NEW
        res.setItems(null);
        return res;
    }

    private OrderResponse toResponseDetailed(SalesOrder order) {
        OrderResponse res = toResponseSummary(order);
        res.setItems(toItemResponses(order.getItems()));
        return res;
    }

    private List<OrderItemResponse> toItemResponses(List<OrderItem> items) {
        List<OrderItemResponse> list = new ArrayList<>();
        for (OrderItem item : items) {
            Product p = item.getProduct();
            list.add(new OrderItemResponse(
                    p.getId(), p.getName(), p.getUnit(),
                    item.getQuantity(), item.getSellingPrice(),
                    item.getCostPrice(), item.getTotalPrice(), item.getProfit()
            ));
        }
        return list;
    }
}