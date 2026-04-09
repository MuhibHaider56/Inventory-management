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
public class SalesReturnService {

    private final SalesReturnRepository returnRepo;
    private final SalesOrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final InventoryRepository inventoryRepo;
    private final AuditService auditService;

    public SalesReturnService(SalesReturnRepository returnRepo,
                              SalesOrderRepository orderRepo,
                              OrderItemRepository itemRepo,
                              InventoryRepository inventoryRepo,
                              AuditService auditService) {
        this.returnRepo = returnRepo;
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.inventoryRepo = inventoryRepo;
        this.auditService = auditService;
    }

    @Transactional
    public ReturnResponse createReturn(ReturnCreateRequest req) {
        SalesOrder order = orderRepo.findDetailedById(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: id=" + req.getOrderId()));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot return items from a cancelled order");
        }
        if (order.isDeleted()) {
            throw new BadRequestException("Cannot return items from a deleted order");
        }

        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setOrder(order);
        salesReturn.setReason(req.getReason());
        salesReturn.setCreatedBy(auditService.getCurrentUsername());

        BigDecimal totalRefund = BigDecimal.ZERO;

        for (ReturnItemRequest itemReq : req.getItems()) {
            OrderItem orderItem = itemRepo.findById(itemReq.getOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "OrderItem not found: id=" + itemReq.getOrderItemId()));

            if (!orderItem.getOrder().getId().equals(order.getId())) {
                throw new BadRequestException("OrderItem " + itemReq.getOrderItemId()
                        + " does not belong to order " + order.getId());
            }

            BigDecimal returnQty = MoneyUtil.scale2(itemReq.getQuantity());
            BigDecimal alreadyReturned = returnRepo.sumReturnedQtyByOrderItemId(itemReq.getOrderItemId());
            BigDecimal remaining = MoneyUtil.subtract(orderItem.getQuantity(), alreadyReturned);
            if (returnQty.compareTo(remaining) > 0) {
                throw new BadRequestException("Return quantity (" + returnQty
                        + ") exceeds remaining returnable quantity (" + remaining
                        + ") for product: " + orderItem.getProduct().getName());
            }

            // Calculate refund: returnQty * sellingPrice
            BigDecimal lineRefund = MoneyUtil.multiply(orderItem.getSellingPrice(), returnQty);

            SalesReturnItem returnItem = new SalesReturnItem();
            returnItem.setOrderItem(orderItem);
            returnItem.setProduct(orderItem.getProduct());
            returnItem.setQuantity(returnQty);
            returnItem.setRefundAmount(lineRefund);
            returnItem.setRestock(itemReq.isRestock());
            salesReturn.addItem(returnItem);

            totalRefund = MoneyUtil.add(totalRefund, lineRefund);

            // Restock inventory if requested
            if (itemReq.isRestock()) {
                Inventory inv = inventoryRepo.findByProductIdForUpdate(orderItem.getProduct().getId())
                        .orElse(null);
                if (inv != null) {
                    inv.setQuantityAvailable(MoneyUtil.add(inv.getQuantityAvailable(), returnQty));
                    inventoryRepo.save(inv);
                }
            }
        }

        salesReturn.setRefundAmount(totalRefund);
        salesReturn.setRefundStatus(RefundStatus.PENDING);

        SalesReturn saved = returnRepo.save(salesReturn);

        // Update order totals
        order.setTotalReturns(MoneyUtil.add(order.getTotalReturns(), totalRefund));
        order.setTotalAmount(MoneyUtil.subtract(order.getTotalAmount(), totalRefund));
        // Recalculate profit: reduce by (returnQty * (sellingPrice - costPrice)) for each item
        BigDecimal profitReduction = BigDecimal.ZERO;
        for (SalesReturnItem ri : saved.getItems()) {
            BigDecimal marginPerUnit = MoneyUtil.subtract(
                    ri.getOrderItem().getSellingPrice(), ri.getOrderItem().getCostPrice());
            profitReduction = MoneyUtil.add(profitReduction,
                    MoneyUtil.multiply(marginPerUnit, ri.getQuantity()));
        }
        order.setTotalProfit(MoneyUtil.subtract(order.getTotalProfit(), profitReduction));
        order.setPaymentStatus(calcPaymentStatus(order));
        orderRepo.save(order);

        auditService.log("RETURN", saved.getId(), "CREATE",
                "Return created for order #" + order.getId()
                        + ", refund=" + totalRefund
                        + ", items=" + saved.getItems().size());

        return toResponse(saved);
    }

    @Transactional
    public ReturnResponse markRefunded(Long returnId) {
        SalesReturn sr = returnRepo.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found: id=" + returnId));

        if (sr.getRefundStatus() == RefundStatus.REFUNDED) {
            throw new BadRequestException("Return is already refunded");
        }

        sr.setRefundStatus(RefundStatus.REFUNDED);
        returnRepo.save(sr);

        // Reduce amountPaid on the order by refund amount
        SalesOrder order = sr.getOrder();
        order.setAmountPaid(MoneyUtil.subtract(order.getAmountPaid(), sr.getRefundAmount()));
        if (order.getAmountPaid().compareTo(BigDecimal.ZERO) < 0) {
            order.setAmountPaid(BigDecimal.ZERO);
        }
        order.setPaymentStatus(calcPaymentStatus(order));
        orderRepo.save(order);

        auditService.log("RETURN", returnId, "REFUNDED",
                "Refund of " + sr.getRefundAmount() + " processed for order #" + order.getId());

        return toResponse(sr);
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> getByOrder(Long orderId) {
        return returnRepo.findByOrderIdOrderByReturnDateDesc(orderId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<ReturnResponse> listAll(Pageable pageable) {
        return returnRepo.findAllByOrderByReturnDateDesc(pageable).map(this::toResponse);
    }

    private PaymentStatus calcPaymentStatus(SalesOrder o) {
        if (o.getAmountPaid().compareTo(BigDecimal.ZERO) == 0) return PaymentStatus.UNPAID;
        if (o.getAmountPaid().compareTo(o.getTotalAmount()) < 0) return PaymentStatus.PARTIALLY_PAID;
        return PaymentStatus.PAID;
    }

    private ReturnResponse toResponse(SalesReturn sr) {
        ReturnResponse res = new ReturnResponse();
        res.setId(sr.getId());
        res.setOrderId(sr.getOrder().getId());
        res.setInvoiceNumber(sr.getOrder().getInvoiceNumber());
        res.setReturnDate(sr.getReturnDate());
        res.setReason(sr.getReason());
        res.setRefundAmount(sr.getRefundAmount());
        res.setRefundStatus(sr.getRefundStatus().name());
        res.setCreatedBy(sr.getCreatedBy());

        List<ReturnItemResponse> itemResponses = new ArrayList<>();
        for (SalesReturnItem item : sr.getItems()) {
            ReturnItemResponse ir = new ReturnItemResponse();
            ir.setOrderItemId(item.getOrderItem().getId());
            ir.setProductName(item.getProduct().getName());
            ir.setUnit(item.getProduct().getUnit());
            ir.setQuantity(item.getQuantity());
            ir.setRefundAmount(item.getRefundAmount());
            ir.setRestocked(item.isRestock());
            itemResponses.add(ir);
        }
        res.setItems(itemResponses);
        return res;
    }
}
