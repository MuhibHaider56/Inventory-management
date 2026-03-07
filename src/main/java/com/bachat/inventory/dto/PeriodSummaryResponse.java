package com.bachat.inventory.dto;

import java.math.BigDecimal;

public class PeriodSummaryResponse {

    private String period;  // "2026-03", "2026-W11", "2026"
    private BigDecimal totalSales;
    private BigDecimal totalProfit;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
    private BigDecimal totalPaid;
    private BigDecimal totalOutstanding;
    private long orderCount;
    private BigDecimal profitMarginPercent;  // (netProfit / totalSales) * 100

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }

    public BigDecimal getTotalProfit() { return totalProfit; }
    public void setTotalProfit(BigDecimal totalProfit) { this.totalProfit = totalProfit; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public BigDecimal getNetProfit() { return netProfit; }
    public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }

    public BigDecimal getTotalPaid() { return totalPaid; }
    public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }

    public BigDecimal getTotalOutstanding() { return totalOutstanding; }
    public void setTotalOutstanding(BigDecimal totalOutstanding) { this.totalOutstanding = totalOutstanding; }

    public long getOrderCount() { return orderCount; }
    public void setOrderCount(long orderCount) { this.orderCount = orderCount; }

    public BigDecimal getProfitMarginPercent() { return profitMarginPercent; }
    public void setProfitMarginPercent(BigDecimal profitMarginPercent) { this.profitMarginPercent = profitMarginPercent; }
}
