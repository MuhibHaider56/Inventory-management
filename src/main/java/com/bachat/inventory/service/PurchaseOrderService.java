package com.bachat.inventory.service;

import com.bachat.inventory.domain.*;
import com.bachat.inventory.dto.*;
import com.bachat.inventory.exception.BadRequestException;
import com.bachat.inventory.exception.ResourceNotFoundException;
import com.bachat.inventory.repository.*;
import com.bachat.inventory.util.MoneyUtil;
import com.bachat.inventory.util.UnitConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepo;
    private final PurchaseOrderItemRepository poItemRepo;
    private final PurchaseOrderPaymentRepository poPaymentRepo;
    private final SupplierRepository supplierRepo;
    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;
    private final AuditService auditService;

    private static final AtomicLong poCounter = new AtomicLong(System.currentTimeMillis() % 10000);

    public PurchaseOrderService(PurchaseOrderRepository poRepo,
                                PurchaseOrderItemRepository poItemRepo,
                                PurchaseOrderPaymentRepository poPaymentRepo,
                                SupplierRepository supplierRepo,
                                ProductRepository productRepo,
                                InventoryRepository inventoryRepo,
                                AuditService auditService) {
        this.poRepo = poRepo;
        this.poItemRepo = poItemRepo;
        this.poPaymentRepo = poPaymentRepo;
        this.supplierRepo = supplierRepo;
        this.productRepo = productRepo;
        this.inventoryRepo = inventoryRepo;
        this.auditService = auditService;
    }

    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderCreateRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BadRequestException("Purchase order items must not be empty");
        }

        Supplier supplier = supplierRepo.findById(req.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: id=" + req.getSupplierId()));
        if (supplier.isDeleted()) throw new BadRequestException("Supplier has been deleted");

        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setStatus(PurchaseOrderStatus.RECEIVED);
        po.setCreatedBy(auditService.getCurrentUsername());
        po.setNotes(req.getNotes());
        po.setPoNumber(generatePoNumber());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PurchaseOrderItemRequest itemReq : req.getItems()) {
            Product product = productRepo.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: id=" + itemReq.getProductId()));
            if (product.isDeleted()) throw new BadRequestException("Product deleted: " + product.getName());

            BigDecimal qty = MoneyUtil.scale2(
                    UnitConverter.toProductUnit(itemReq.getQuantity(), itemReq.getUnit(), product.getUnit()));
            BigDecimal unitPrice = MoneyUtil.scale2(itemReq.getUnitPrice());
            BigDecimal lineTotal = MoneyUtil.multiply(unitPrice, qty);

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProduct(product);
            item.setQuantity(qty);
            item.setUnitPrice(unitPrice);
            item.setTotalPrice(lineTotal);
            po.addItem(item);

            totalAmount = MoneyUtil.add(totalAmount, lineTotal);

            // Add stock to inventory
            Inventory inv = inventoryRepo.findByProductIdForUpdate(product.getId()).orElse(null);
            if (inv == null) {
                inv = new Inventory(product, BigDecimal.ZERO);
            }
            inv.setQuantityAvailable(MoneyUtil.add(inv.getQuantityAvailable(), qty));
            inventoryRepo.save(inv);
        }

        po.setTotalAmount(MoneyUtil.scale2(totalAmount));

        // Handle initial payment
        BigDecimal paid = req.getAmountPaid() != null ? MoneyUtil.scale2(req.getAmountPaid()) : BigDecimal.ZERO;
        if (paid.compareTo(MoneyUtil.scale2(totalAmount)) > 0) {
            throw new BadRequestException("amountPaid cannot exceed totalAmount");
        }
        po.setAmountPaid(paid);
        po.setPaymentMethod(req.getPaymentMethod());
        po.setPaymentReference(req.getPaymentReference());
        po.setPaymentDate(req.getPaymentDate());
        po.setPaymentDueDate(req.getPaymentDueDate());
        po.setPaymentStatus(resolvePaymentStatus(paid, MoneyUtil.scale2(totalAmount)));

        PurchaseOrder saved = poRepo.save(po);

        // Record initial payment in history if any amount paid
        if (paid.compareTo(BigDecimal.ZERO) > 0) {
            PurchaseOrderPayment payment = new PurchaseOrderPayment();
            payment.setPurchaseOrder(saved);
            payment.setAmount(paid);
            payment.setMethod(req.getPaymentMethod());
            payment.setReference(req.getPaymentReference());
            payment.setNote("Initial payment on PO creation");
            payment.setRecordedBy(auditService.getCurrentUsername());
            poPaymentRepo.save(payment);
        }

        // Recalculate weighted average cost for each product
        for (PurchaseOrderItem item : saved.getItems()) {
            recalculateWAC(item.getProduct().getId());
        }

        auditService.log("PURCHASE_ORDER", saved.getId(), "CREATE",
                "PO " + saved.getPoNumber() + " from supplier " + supplier.getName()
                        + ", total=" + saved.getTotalAmount()
                        + ", items=" + saved.getItems().size());

        return toDetailedResponse(saved);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse get(Long id) {
        PurchaseOrder po = poRepo.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: id=" + id));
        return toDetailedResponse(po);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> list(Pageable pageable) {
        return poRepo.findAllWithSupplier(pageable).map(this::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> listBySupplier(Long supplierId, Pageable pageable) {
        return poRepo.findBySupplierId(supplierId, pageable).map(this::toSummaryResponse);
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long id) {
        PurchaseOrder po = poRepo.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: id=" + id));

        if (po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BadRequestException("Purchase order is already cancelled");
        }

        // Reverse inventory
        for (PurchaseOrderItem item : po.getItems()) {
            Inventory inv = inventoryRepo.findByProductIdForUpdate(item.getProduct().getId()).orElse(null);
            if (inv != null) {
                BigDecimal newQty = MoneyUtil.subtract(inv.getQuantityAvailable(), item.getQuantity());
                if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BadRequestException("Cannot cancel PO: stock for '"
                            + item.getProduct().getName() + "' would go below 0. "
                            + "Current stock=" + inv.getQuantityAvailable()
                            + ", PO qty=" + item.getQuantity());
                }
                inv.setQuantityAvailable(newQty);
                inventoryRepo.save(inv);
            }
        }

        po.setStatus(PurchaseOrderStatus.CANCELLED);
        PurchaseOrder saved = poRepo.save(po);

        // Recalculate WAC after cancellation
        for (PurchaseOrderItem item : saved.getItems()) {
            recalculateWAC(item.getProduct().getId());
        }

        auditService.log("PURCHASE_ORDER", id, "CANCEL",
                "PO " + po.getPoNumber() + " cancelled, stock reversed");

        return toDetailedResponse(saved);
    }

    @Transactional
    public PurchaseOrderResponse addPayment(Long id, PurchaseOrderPaymentRequest req) {
        PurchaseOrder po = poRepo.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: id=" + id));

        if (po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot add payment to a cancelled purchase order");
        }
        if (po.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Purchase order already fully paid");
        }

        BigDecimal amount = MoneyUtil.scale2(req.getAmount());
        BigDecimal balanceDue = MoneyUtil.subtract(po.getTotalAmount(), po.getAmountPaid());

        if (amount.compareTo(balanceDue) > 0) {
            throw new BadRequestException("Payment amount " + amount + " exceeds balance due " + balanceDue);
        }

        BigDecimal newPaid = MoneyUtil.add(po.getAmountPaid(), amount);
        po.setAmountPaid(newPaid);
        po.setPaymentStatus(resolvePaymentStatus(newPaid, po.getTotalAmount()));
        poRepo.save(po);

        PurchaseOrderPayment payment = new PurchaseOrderPayment();
        payment.setPurchaseOrder(po);
        payment.setAmount(amount);
        payment.setMethod(req.getMethod());
        payment.setReference(req.getReference());
        payment.setNote(req.getNote());
        payment.setRecordedBy(auditService.getCurrentUsername());
        poPaymentRepo.save(payment);

        auditService.log("PURCHASE_ORDER", id, "PAYMENT_ADDED",
                "PO " + po.getPoNumber() + " payment=" + amount
                        + ", newPaid=" + newPaid + ", status=" + po.getPaymentStatus());

        return toDetailedResponse(po);
    }

    @Transactional
    public PurchaseOrderResponse updateDueDate(Long id, LocalDate dueDate) {
        PurchaseOrder po = poRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: id=" + id));

        if (po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot update due date of a cancelled purchase order");
        }

        po.setPaymentDueDate(dueDate);
        PurchaseOrder saved = poRepo.save(po);

        auditService.log("PURCHASE_ORDER", id, "DUE_DATE_UPDATED",
                "PO " + po.getPoNumber() + " paymentDueDate=" + dueDate);

        return toSummaryResponse(saved);
    }

    private PaymentStatus resolvePaymentStatus(BigDecimal paid, BigDecimal total) {
        if (paid.compareTo(BigDecimal.ZERO) == 0) return PaymentStatus.UNPAID;
        if (paid.compareTo(total) >= 0) return PaymentStatus.PAID;
        return PaymentStatus.PARTIALLY_PAID;
    }

    /**
     * Recalculates the weighted average cost for a product based on all RECEIVED purchase orders
     * and updates the product's costPrice.
     */
    private void recalculateWAC(Long productId) {
        Product product = productRepo.findById(productId).orElse(null);
        if (product == null) return;

        List<Object[]> rows = poItemRepo.sumQuantityAndCostByProduct(productId);
        Object[] result = rows.get(0);
        BigDecimal poQty = (BigDecimal) result[0];
        BigDecimal poCost = (BigDecimal) result[1];

        // Include initial stock in WAC calculation
        BigDecimal initQty = product.getInitialStock();
        BigDecimal initCost = MoneyUtil.multiply(initQty, product.getInitialCostPrice());

        BigDecimal totalQty = MoneyUtil.add(poQty, initQty);
        BigDecimal totalCost = MoneyUtil.add(poCost, initCost);

        if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal wac = totalCost.divide(totalQty, 2, RoundingMode.HALF_UP);
            BigDecimal oldCost = product.getCostPrice();
            product.setCostPrice(wac);
            productRepo.save(product);

            auditService.log("PRODUCT", productId, "WAC_UPDATE",
                    "Weighted average cost recalculated",
                    oldCost.toPlainString(), wac.toPlainString());
        }
    }

    private String generatePoNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return "PO-" + date + "-" + String.format("%04d", poCounter.incrementAndGet() % 10000);
    }

    private PurchaseOrderResponse toSummaryResponse(PurchaseOrder po) {
        PurchaseOrderResponse r = new PurchaseOrderResponse();
        r.setId(po.getId());
        r.setPoNumber(po.getPoNumber());
        r.setSupplierId(po.getSupplier().getId());
        r.setSupplierName(po.getSupplier().getName());
        r.setPurchaseDate(po.getPurchaseDate());
        r.setStatus(po.getStatus().name());
        r.setTotalAmount(po.getTotalAmount());
        r.setAmountPaid(po.getAmountPaid());
        r.setBalanceDue(MoneyUtil.subtract(po.getTotalAmount(), po.getAmountPaid()));
        r.setPaymentStatus(po.getPaymentStatus() != null ? po.getPaymentStatus().name() : PaymentStatus.UNPAID.name());
        r.setPaymentMethod(po.getPaymentMethod());
        r.setPaymentDueDate(po.getPaymentDueDate());
        r.setCreatedBy(po.getCreatedBy());
        return r;
    }

    private PurchaseOrderResponse toDetailedResponse(PurchaseOrder po) {
        PurchaseOrderResponse r = toSummaryResponse(po);
        r.setPaymentReference(po.getPaymentReference());
        r.setPaymentDate(po.getPaymentDate());
        r.setNotes(po.getNotes());

        List<PurchaseOrderItemResponse> items = new ArrayList<>();
        for (PurchaseOrderItem item : po.getItems()) {
            PurchaseOrderItemResponse ir = new PurchaseOrderItemResponse();
            ir.setProductId(item.getProduct().getId());
            ir.setProductName(item.getProduct().getName());
            ir.setUnit(item.getProduct().getUnit());
            ir.setQuantity(item.getQuantity());
            ir.setUnitPrice(item.getUnitPrice());
            ir.setTotalPrice(item.getTotalPrice());
            items.add(ir);
        }
        r.setItems(items);

        List<PurchaseOrderPaymentResponse> payments = new ArrayList<>();
        for (PurchaseOrderPayment p : poPaymentRepo.findByPurchaseOrderId(po.getId())) {
            PurchaseOrderPaymentResponse pr = new PurchaseOrderPaymentResponse();
            pr.setId(p.getId());
            pr.setAmount(p.getAmount());
            pr.setPaymentDate(p.getPaymentDate());
            pr.setMethod(p.getMethod());
            pr.setReference(p.getReference());
            pr.setNote(p.getNote());
            pr.setRecordedBy(p.getRecordedBy());
            payments.add(pr);
        }
        r.setPayments(payments);

        return r;
    }
}
