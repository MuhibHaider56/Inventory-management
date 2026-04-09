package com.bachat.inventory.dto;

import java.math.BigDecimal;

public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private BigDecimal totalPrice;
    private BigDecimal profit;

    public OrderItemResponse() {}

    public OrderItemResponse(Long productId, String productName, String unit, BigDecimal quantity,
                             BigDecimal sellingPrice, BigDecimal costPrice, BigDecimal totalPrice, BigDecimal profit) {
        this.productId = productId;
        this.productName = productName;
        this.unit = unit;
        this.quantity = quantity;
        this.sellingPrice = sellingPrice;
        this.costPrice = costPrice;
        this.totalPrice = totalPrice;
        this.profit = profit;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public BigDecimal getProfit() {
        return profit;
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

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }
}
