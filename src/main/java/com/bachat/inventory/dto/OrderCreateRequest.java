package com.bachat.inventory.dto;

import com.bachat.inventory.domain.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OrderCreateRequest {

    @NotNull(message = "customerId is required")
    private Long customerId;

    private OrderStatus status = OrderStatus.COMPLETED;

    @NotEmpty(message = "items must not be empty")
    @Valid
    private List<OrderItemRequest> items;

    @PositiveOrZero(message = "amountPaid must be >= 0")
    private BigDecimal amountPaid = BigDecimal.ZERO;

    private LocalDate paymentDueDate;
    private String paymentMethod;
    private String paymentReference;
    private String paymentNote;
    // AFTER
    @Valid
    private List<ExpenseCreateRequest> expenses;

    public List<ExpenseCreateRequest> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<ExpenseCreateRequest> expenses) {
        this.expenses = expenses;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getPaymentNote() {
        return paymentNote;
    }

    public void setPaymentNote(String paymentNote) {
        this.paymentNote = paymentNote;
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public LocalDate getPaymentDueDate() { return paymentDueDate; }
    public void setPaymentDueDate(LocalDate paymentDueDate) { this.paymentDueDate = paymentDueDate; }
}