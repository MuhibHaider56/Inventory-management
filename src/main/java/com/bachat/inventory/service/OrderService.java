package com.bachat.inventory.service;

import com.bachat.inventory.domain.*;
import com.bachat.inventory.dto.*;
import com.bachat.inventory.exception.BadRequestException;
import com.bachat.inventory.exception.ResourceNotFoundException;
import com.bachat.inventory.repository.*;
import com.bachat.inventory.util.InvoiceNumberUtil;
import com.bachat.inventory.util.MoneyUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final SalesOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderPaymentRepository orderPaymentRepository;

    public OrderService(SalesOrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        InventoryRepository inventoryRepository,
                        OrderPaymentRepository orderPaymentRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderPaymentRepository = orderPaymentRepository;
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

            // Lock inventory row for this product
            Inventory inv = inventoryRepository.findByProductIdForUpdate(product.getId())
                    .orElseThrow(() -> new BadRequestException("No inventory record for productId=" + product.getId() + ". Create product properly or add stock first."));

            if (inv.getQuantityAvailable().compareTo(qty) < 0) {
                throw new BadRequestException("Not enough stock for product '" + product.getName() +
                        "'. Available=" + inv.getQuantityAvailable() + " " + product.getUnit() +
                        ", requested=" + qty + " " + product.getUnit());
            }

            // Deduct stock
            inv.setQuantityAvailable(MoneyUtil.subtract(inv.getQuantityAvailable(), qty));
            inventoryRepository.save(inv);


            BigDecimal sellingPrice = itemReq.getSellingPrice() != null
                    ? MoneyUtil.scale2(itemReq.getSellingPrice())
                    : MoneyUtil.scale2(product.getSellingPrice());

            BigDecimal costPrice = MoneyUtil.scale2(product.getCostPrice());

            BigDecimal lineTotal = MoneyUtil.multiply(sellingPrice, qty);
            BigDecimal lineProfit = MoneyUtil.multiply(MoneyUtil.subtract(sellingPrice, costPrice), qty);
// Optional safety check
            if (sellingPrice.compareTo(costPrice) < 0) {
                throw new BadRequestException("Selling price cannot be below cost price for product: " + product.getName());
            }
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

        order.setTotalAmount(MoneyUtil.scale2(totalAmount));
        order.setTotalProfit(MoneyUtil.scale2(totalProfit));

        SalesOrder saved = orderRepository.save(order);
        return toResponseDetailed(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        SalesOrder order = orderRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: id=" + id));
        return toResponseDetailed(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> list(Pageable pageable) {
        // List without eager items to keep it light: items are not included.
        return orderRepository.findAll(pageable).map(this::toResponseSummary);
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        SalesOrder order = orderRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: id=" + id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }

        // Restock all items
        for (OrderItem item : order.getItems()) {
            Long productId = item.getProduct().getId();
            Inventory inv = inventoryRepository.findByProductIdForUpdate(productId)
                    .orElseThrow(() -> new BadRequestException("Inventory missing for productId=" + productId));

            inv.setQuantityAvailable(MoneyUtil.add(inv.getQuantityAvailable(), item.getQuantity()));
            inventoryRepository.save(inv);
        }

        order.setStatus(OrderStatus.CANCELLED);
        SalesOrder saved = orderRepository.save(order);

        return toResponseDetailed(saved);
    }

    public InvoiceResponse getInvoice(Long orderId) {

        SalesOrder order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        InvoiceResponse r = new InvoiceResponse();

        r.setOrderId(order.getId());
        r.setCustomerName(order.getCustomer().getName());
        r.setOrderDate(order.getOrderDate());
        r.setPaymentDueDate(order.getPaymentDueDate());
        r.setPaymentStatus(order.getPaymentStatus().name());

        r.setTotalAmount(order.getTotalAmount());
        r.setAmountPaid(order.getAmountPaid());
        r.setBalanceDue(
                order.getTotalAmount().subtract(order.getAmountPaid())
        );

        // Line items
        List<InvoiceItem> items = order.getItems()
                .stream()
                .map(oi -> {
                    InvoiceItem ii = new InvoiceItem();
                    ii.setProduct(oi.getProduct().getName());
                    ii.setQuantity(oi.getQuantity());
                    ii.setUnit(oi.getProduct().getUnit());
                    ii.setSellingPrice(oi.getSellingPrice());
                    ii.setTotalPrice(oi.getTotalPrice());
                    return ii;
                })
                .toList();

        r.setItems(items);

        // Payments
        List<InvoicePayment> payments =
                orderPaymentRepository
                        .findByOrderIdOrderByPaymentDateAsc(orderId)
                        .stream()
                        .map(p -> {
                            InvoicePayment ip = new InvoicePayment();
                            ip.setAmount(p.getAmount());
                            ip.setPaymentDate(p.getPaymentDate());
                            ip.setMethod(p.getMethod());
                            ip.setReference(p.getReference());
                            return ip;
                        })
                        .toList();

        r.setPayments(payments);

        return r;
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
        res.setItems(null); // intentionally omitted for list endpoint
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
                    p.getId(),
                    p.getName(),
                    p.getUnit(),
                    item.getQuantity(),
                    item.getSellingPrice(),
                    item.getCostPrice(),
                    item.getTotalPrice(),
                    item.getProfit()
            ));
        }
        return list;
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

        if (amount.compareTo(remaining) <= 0 == false) {
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

}
