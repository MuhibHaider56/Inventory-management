package com.bachat.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class ReturnItemRequest {

    @NotNull(message = "orderItemId is required")
    private Long orderItemId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be > 0")
    private BigDecimal quantity;

    private boolean restock = true;

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public boolean isRestock() { return restock; }
    public void setRestock(boolean restock) { this.restock = restock; }
}
