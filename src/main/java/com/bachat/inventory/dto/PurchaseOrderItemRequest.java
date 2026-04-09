package com.bachat.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class PurchaseOrderItemRequest {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be > 0")
    private BigDecimal quantity;

    @NotNull(message = "unitPrice is required")
    @Positive(message = "unitPrice must be > 0")
    private BigDecimal unitPrice;

    /**
     * Optional. Unit of the quantity being sent (e.g. "g", "kg", "maund").
     * If omitted, the product's own unit is assumed (backward-compatible).
     */
    private String unit;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
