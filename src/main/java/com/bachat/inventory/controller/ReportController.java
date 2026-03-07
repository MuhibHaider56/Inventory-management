package com.bachat.inventory.controller;

import com.bachat.inventory.dto.*;
import com.bachat.inventory.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    @GetMapping("/sales-summary")
    public SalesSummaryResponse getSummary(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {

        return reportService.getSalesSummary(start, end);
    }
    @GetMapping("/today-summary")
    public SalesSummaryResponse todaySummary() {
        return reportService.getTodaySummary();
    }
    @GetMapping("/overdue")
    public List<OverdueResponse> overdue() {
        return reportService.getOverdue();
    }
    @GetMapping("/customer-performance")
    public List<CustomerPerformanceResponse> customerPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        return reportService.getCustomerPerformance(start, end);
    }
    @GetMapping("/product-profitability")
    public List<ProductProfitResponse> productProfitability(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        return reportService.getProductProfitability(start, end);
    }
    @GetMapping("/daily-collections")
    public List<DailyCollectionResponse> dailyCollections(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        return reportService.getDailyCollections(start, end);
    }
}
