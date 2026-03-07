package com.bachat.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ReturnResponse {

    private Long id;
    private Long orderId;
    private String invoiceNumber;
    private LocalDateTime returnDate;
    private String reason;
    private BigDecimal refundAmount;
    private String refundStatus;
    private String createdBy;
    private List<ReturnItemResponse> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getRefundStatus() { return refundStatus; }
    public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public List<ReturnItemResponse> getItems() { return items; }
    public void setItems(List<ReturnItemResponse> items) { this.items = items; }
}
