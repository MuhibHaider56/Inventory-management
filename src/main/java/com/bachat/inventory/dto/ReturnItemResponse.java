package com.bachat.inventory.dto;

import java.math.BigDecimal;

public class ReturnItemResponse {

    private Long orderItemId;
    private String productName;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal refundAmount;
    private boolean restocked;

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public boolean isRestocked() { return restocked; }
    public void setRestocked(boolean restocked) { this.restocked = restocked; }
}
