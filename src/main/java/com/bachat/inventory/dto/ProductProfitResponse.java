package com.bachat.inventory.dto;

import java.math.BigDecimal;

public class ProductProfitResponse {

    private Long productId;        // NEW: safe grouping
    private String product;
    private String unit;           // NEW: e.g. kg, pcs
    private BigDecimal totalQuantity;
    private BigDecimal totalRevenue;  // NEW: sum of totalPrice
    private BigDecimal totalProfit;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getTotalProfit() { return totalProfit; }
    public void setTotalProfit(BigDecimal totalProfit) { this.totalProfit = totalProfit; }
}