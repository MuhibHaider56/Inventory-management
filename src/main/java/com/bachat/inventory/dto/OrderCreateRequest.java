package com.bachat.inventory.dto;

import com.bachat.inventory.domain.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderCreateRequest {

    @NotNull(message = "customerId is required")
    private Long customerId;

    private OrderStatus status = OrderStatus.COMPLETED;

    @NotEmpty(message = "items must not be empty")
    @Valid
    private List<OrderItemRequest> items;

    public Long getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
