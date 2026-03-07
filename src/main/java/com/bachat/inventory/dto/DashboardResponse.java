package com.bachat.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {

    private BigDecimal todaySales;
    private BigDecimal todayProfit;
    private BigDecimal todayExpenses;
    private BigDecimal todayCollections;
    private long todayOrderCount;

    private long overdueCount;
    private BigDecimal totalOverdueAmount;

    private long lowStockCount;
    private BigDecimal totalOutstanding;

    private List<OrderResponse> recentOrders;

    public BigDecimal getTodaySales() { return todaySales; }
    public void setTodaySales(BigDecimal todaySales) { this.todaySales = todaySales; }

    public BigDecimal getTodayProfit() { return todayProfit; }
    public void setTodayProfit(BigDecimal todayProfit) { this.todayProfit = todayProfit; }

    public BigDecimal getTodayExpenses() { return todayExpenses; }
    public void setTodayExpenses(BigDecimal todayExpenses) { this.todayExpenses = todayExpenses; }

    public BigDecimal getTodayCollections() { return todayCollections; }
    public void setTodayCollections(BigDecimal todayCollections) { this.todayCollections = todayCollections; }

    public long getTodayOrderCount() { return todayOrderCount; }
    public void setTodayOrderCount(long todayOrderCount) { this.todayOrderCount = todayOrderCount; }

    public long getOverdueCount() { return overdueCount; }
    public void setOverdueCount(long overdueCount) { this.overdueCount = overdueCount; }

    public BigDecimal getTotalOverdueAmount() { return totalOverdueAmount; }
    public void setTotalOverdueAmount(BigDecimal totalOverdueAmount) { this.totalOverdueAmount = totalOverdueAmount; }

    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }

    public BigDecimal getTotalOutstanding() { return totalOutstanding; }
    public void setTotalOutstanding(BigDecimal totalOutstanding) { this.totalOutstanding = totalOutstanding; }

    public List<OrderResponse> getRecentOrders() { return recentOrders; }
    public void setRecentOrders(List<OrderResponse> recentOrders) { this.recentOrders = recentOrders; }
}
