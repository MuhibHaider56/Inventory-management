package com.bachat.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ReturnCreateRequest {

    @NotNull(message = "orderId is required")
    private Long orderId;

    private String reason;

    @NotEmpty(message = "items must not be empty")
    @Valid
    private List<ReturnItemRequest> items;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public List<ReturnItemRequest> getItems() { return items; }
    public void setItems(List<ReturnItemRequest> items) { this.items = items; }
}
