package com.bachat.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyCollectionResponse {

    private LocalDate date;
    private BigDecimal totalCollected;
    private long transactionCount;  // how many payment records that day
    private long orderCount;        // how many distinct orders paid that day

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getTotalCollected() { return totalCollected; }
    public void setTotalCollected(BigDecimal totalCollected) { this.totalCollected = totalCollected; }

    public long getTransactionCount() { return transactionCount; }
    public void setTransactionCount(long transactionCount) { this.transactionCount = transactionCount; }

    public long getOrderCount() { return orderCount; }
    public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
}