package com.bachat.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReportSummaryResponse {

    private LocalDate start;
    private LocalDate end;

    private BigDecimal totalSales;
    private BigDecimal totalGrossProfit;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;

    public ReportSummaryResponse() {}

    public ReportSummaryResponse(LocalDate start, LocalDate end, BigDecimal totalSales,
                                 BigDecimal totalGrossProfit, BigDecimal totalExpenses, BigDecimal netProfit) {
        this.start = start;
        this.end = end;
        this.totalSales = totalSales;
        this.totalGrossProfit = totalGrossProfit;
        this.totalExpenses = totalExpenses;
        this.netProfit = netProfit;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public BigDecimal getTotalGrossProfit() {
        return totalGrossProfit;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public void setTotalGrossProfit(BigDecimal totalGrossProfit) {
        this.totalGrossProfit = totalGrossProfit;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }
}
