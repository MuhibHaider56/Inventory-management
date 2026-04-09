package com.bachat.inventory.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class PurchaseOrderDueDateRequest {

    @NotNull(message = "paymentDueDate is required")
    private LocalDate paymentDueDate;

    public LocalDate getPaymentDueDate() { return paymentDueDate; }
    public void setPaymentDueDate(LocalDate paymentDueDate) { this.paymentDueDate = paymentDueDate; }
}
