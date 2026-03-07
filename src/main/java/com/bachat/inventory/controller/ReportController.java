package com.bachat.inventory.controller;

import com.bachat.inventory.dto.*;
import com.bachat.inventory.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Reports", description = "Business reports, analytics, dashboards, and trends.")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ==================== Dashboard ====================

    @Operation(summary = "Dashboard summary",
               description = "Single call returning today's sales/profit/expenses, overdue count, low stock count, total outstanding, and 5 recent orders.")
    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return reportService.getDashboard();
    }

    // ==================== Sales Summary ====================

    @Operation(summary = "Sales summary for a date range")
    @GetMapping("/sales-summary")
    public SalesSummaryResponse getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return reportService.getSalesSummary(start, end);
    }

    @Operation(summary = "Today's sales summary")
    @GetMapping("/today-summary")
    public SalesSummaryResponse todaySummary() {
        return reportService.getTodaySummary();
    }

    // ==================== Trends ====================

    @Operation(summary = "Monthly sales/profit trend",
               description = "Returns aggregated data per month within the date range. Includes profit margin percentage.")
    @GetMapping("/trend/monthly")
    public List<PeriodSummaryResponse> monthlyTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return reportService.getMonthlyTrend(start, end);
    }

    @Operation(summary = "Weekly sales/profit trend")
    @GetMapping("/trend/weekly")
    public List<PeriodSummaryResponse> weeklyTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return reportService.getWeeklyTrend(start, end);
    }

    @Operation(summary = "Yearly sales/profit trend")
    @GetMapping("/trend/yearly")
    public List<PeriodSummaryResponse> yearlyTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return reportService.getYearlyTrend(start, end);
    }

    // ==================== Existing Reports ====================

    @Operation(summary = "Overdue payments list")
    @GetMapping("/overdue")
    public List<OverdueResponse> overdue() {
        return reportService.getOverdue();
    }

    @Operation(summary = "Customer performance (sales, payments, profit per customer)")
    @GetMapping("/customer-performance")
    public List<CustomerPerformanceResponse> customerPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return reportService.getCustomerPerformance(start, end);
    }

    @Operation(summary = "Product profitability ranking")
    @GetMapping("/product-profitability")
    public List<ProductProfitResponse> productProfitability(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return reportService.getProductProfitability(start, end);
    }

    @Operation(summary = "Daily payment collections breakdown")
    @GetMapping("/daily-collections")
    public List<DailyCollectionResponse> dailyCollections(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return reportService.getDailyCollections(start, end);
    }

    // ==================== Customer Ledger ====================

    @Operation(summary = "Customer ledger / account statement",
               description = "Full account statement for a customer showing all orders (debit), payments (credit), returns (credit), and running balance.")
    @GetMapping("/customer-ledger/{customerId}")
    public CustomerLedgerResponse customerLedger(@PathVariable Long customerId) {
        return reportService.getCustomerLedger(customerId);
    }
}
