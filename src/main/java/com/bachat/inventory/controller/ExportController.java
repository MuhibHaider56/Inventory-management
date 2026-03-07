package com.bachat.inventory.controller;

import com.bachat.inventory.dto.*;
import com.bachat.inventory.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Export", description = "Download reports as PDF or Excel files.")
@RestController
@RequestMapping("/api/v1/export")
public class ExportController {

    private final ReportService reportService;
    private final OrderService orderService;
    private final ExcelExportService excelService;
    private final PdfExportService pdfService;

    public ExportController(ReportService reportService,
                            OrderService orderService,
                            ExcelExportService excelService,
                            PdfExportService pdfService) {
        this.reportService = reportService;
        this.orderService = orderService;
        this.excelService = excelService;
        this.pdfService = pdfService;
    }

    // ==================== PDF Exports ====================

    @Operation(summary = "Download invoice PDF for an order")
    @GetMapping("/invoice/{orderId}/pdf")
    public ResponseEntity<byte[]> invoicePdf(@PathVariable Long orderId) throws Exception {
        InvoiceResponse invoice = orderService.getInvoice(orderId);
        byte[] pdf = pdfService.generateInvoicePdf(invoice);
        String filename = (invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "invoice-" + orderId) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @Operation(summary = "Download customer ledger / account statement as PDF")
    @GetMapping("/customer-ledger/{customerId}/pdf")
    public ResponseEntity<byte[]> ledgerPdf(@PathVariable Long customerId) throws Exception {
        CustomerLedgerResponse ledger = reportService.getCustomerLedger(customerId);
        byte[] pdf = pdfService.generateLedgerPdf(ledger);
        String filename = "ledger-" + ledger.getCustomerName().replaceAll("\\s+", "-") + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ==================== Excel Exports ====================

    @Operation(summary = "Download sales summary as Excel")
    @GetMapping("/sales-summary/excel")
    public ResponseEntity<byte[]> salesSummaryExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) throws Exception {
        SalesSummaryResponse summary = reportService.getSalesSummary(start, end);
        String period = start.toLocalDate() + " to " + end.toLocalDate();
        byte[] excel = excelService.exportSalesSummary(summary, period);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales-summary.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @Operation(summary = "Download customer performance as Excel")
    @GetMapping("/customer-performance/excel")
    public ResponseEntity<byte[]> customerPerformanceExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) throws Exception {
        List<CustomerPerformanceResponse> data = reportService.getCustomerPerformance(start, end);
        byte[] excel = excelService.exportCustomerPerformance(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customer-performance.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @Operation(summary = "Download product profitability as Excel")
    @GetMapping("/product-profitability/excel")
    public ResponseEntity<byte[]> productProfitabilityExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) throws Exception {
        List<ProductProfitResponse> data = reportService.getProductProfitability(start, end);
        byte[] excel = excelService.exportProductProfitability(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=product-profitability.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
