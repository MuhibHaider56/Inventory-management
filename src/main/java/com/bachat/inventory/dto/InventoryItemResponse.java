package com.bachat.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryItemResponse {

    private Long productId;
    private String productName;
    private String unit;
    private BigDecimal quantityAvailable;
    private LocalDateTime lastUpdated;

    public InventoryItemResponse() {}

    public InventoryItemResponse(Long productId, String productName, String unit, BigDecimal quantityAvailable, LocalDateTime lastUpdated) {
        this.productId = productId;
        this.productName = productName;
        this.unit = unit;
        this.quantityAvailable = quantityAvailable;
        this.lastUpdated = lastUpdated;
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

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
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

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
