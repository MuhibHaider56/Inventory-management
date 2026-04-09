package com.bachat.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class OrderItemRequest {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be > 0")
    private BigDecimal quantity;

    /**
     * Optional. Unit of the quantity being sent (e.g. "g", "kg", "maund").
     * If omitted, the product's own unit is assumed (backward-compatible).
     */
    private String unit;

    /**
     * Optional override. If not provided, product.sellingPrice will be used.
     */
    private BigDecimal sellingPrice;

    public Long getProductId() {
        return productId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
}
