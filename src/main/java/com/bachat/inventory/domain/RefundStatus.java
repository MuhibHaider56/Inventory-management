package com.bachat.inventory.domain;

public enum RefundStatus {
    PENDING,
    REFUNDED,
    ADJUSTED  // adjusted against future orders / outstanding balance
}
