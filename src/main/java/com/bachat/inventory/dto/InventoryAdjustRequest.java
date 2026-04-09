package com.bachat.inventory.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class InventoryAdjustRequest {

    @NotNull(message = "productId is required")
    private Long productId;

    /**
     * Can be positive (stock-in) or negative (stock-out/manual correction).
     * Orders will normally do stock-out automatically.
     */
    @NotNull(message = "delta is required")
    private BigDecimal delta;

    /**
     * Optional. Unit of the delta being sent (e.g. "g", "kg", "maund").
     * If omitted, the product's own unit is assumed (backward-compatible).
     */
    private String unit;

    public Long getProductId() {
        return productId;
    }

    public BigDecimal getDelta() {
        return delta;
    }

    public String getUnit() {
        return unit;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setDelta(BigDecimal delta) {
        this.delta = delta;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
