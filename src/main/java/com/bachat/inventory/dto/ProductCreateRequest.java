package com.bachat.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class ProductCreateRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "unit is required (e.g., kg, bag, liter)")
    private String unit;

    @NotNull(message = "costPrice is required")
    @PositiveOrZero(message = "costPrice must be >= 0")
    private BigDecimal costPrice;

    @NotNull(message = "sellingPrice is required")
    @PositiveOrZero(message = "sellingPrice must be >= 0")
    private BigDecimal sellingPrice;

    @PositiveOrZero(message = "initialStock must be >= 0")
    private BigDecimal initialStock = BigDecimal.ZERO;

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public BigDecimal getInitialStock() {
        return initialStock;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public void setInitialStock(BigDecimal initialStock) {
        this.initialStock = initialStock;
    }
}
