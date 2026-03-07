package com.bachat.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PurchaseOrderCreateRequest {

    @NotNull(message = "supplierId is required")
    private Long supplierId;

    @NotEmpty(message = "items must not be empty")
    @Valid
    private List<PurchaseOrderItemRequest> items;

    private BigDecimal amountPaid;
    private String paymentMethod;
    private String paymentReference;
    private LocalDate paymentDate;
    private String notes;

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public List<PurchaseOrderItemRequest> getItems() { return items; }
    public void setItems(List<PurchaseOrderItemRequest> items) { this.items = items; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
