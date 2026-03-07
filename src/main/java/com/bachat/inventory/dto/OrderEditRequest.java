package com.bachat.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

public class OrderEditRequest {

    @NotEmpty(message = "items must not be empty")
    @Valid
    private List<OrderItemRequest> items;

    private LocalDate paymentDueDate;

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }

    public LocalDate getPaymentDueDate() { return paymentDueDate; }
    public void setPaymentDueDate(LocalDate paymentDueDate) { this.paymentDueDate = paymentDueDate; }
}
