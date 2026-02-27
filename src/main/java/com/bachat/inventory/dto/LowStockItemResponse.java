package com.bachat.inventory.dto;

import java.math.BigDecimal;

public class LowStockItemResponse {

    private Long productId;
    private String productName;
    private String unit;
    private BigDecimal quantityAvailable;

    public LowStockItemResponse() {}

    public LowStockItemResponse(Long productId, String productName, String unit, BigDecimal quantityAvailable) {
        this.productId = productId;
        this.productName = productName;
        this.unit = unit;
        this.quantityAvailable = quantityAvailable;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setQuantityAvailable(BigDecimal quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }
}
