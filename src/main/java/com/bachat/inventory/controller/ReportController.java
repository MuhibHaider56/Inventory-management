package com.bachat.inventory.controller;

import com.bachat.inventory.dto.*;
import com.bachat.inventory.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

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
    public List<CustomerPerformanceResponse> customerPerformance() {
        return reportService.getCustomerPerformance();
    }
    @GetMapping("/product-profitability")
    public List<ProductProfitResponse> productProfitability() {
        return reportService.getProductProfitability();
    }
    @GetMapping("/daily-collections")
    public List<Map<String,Object>> dailyCollections() {
        return reportService.getDailyCollections();
    }
}
