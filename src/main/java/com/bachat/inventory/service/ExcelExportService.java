package com.bachat.inventory.service;

import com.bachat.inventory.dto.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] exportSalesSummary(SalesSummaryResponse summary, String period) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Sales Summary");
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle moneyStyle = createMoneyStyle(wb);

            int row = 0;
            row = addTitle(sheet, row, "Sales Summary Report — " + period, headerStyle);
            row++;

            String[][] data = {
                {"Total Sales", fmt(summary.getTotalSales())},
                {"Total Profit", fmt(summary.getTotalProfit())},
                {"Total Expenses", fmt(summary.getTotalExpenses())},
                {"Net Profit", fmt(summary.getNetProfit())},
                {"Total Paid", fmt(summary.getTotalPaid())},
                {"Total Outstanding", fmt(summary.getTotalOutstanding())}
            };

            for (String[] d : data) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(d[0]);
                Cell c = r.createCell(1);
                c.setCellValue(Double.parseDouble(d[1]));
                c.setCellStyle(moneyStyle);
            }

            autoSizeColumns(sheet, 2);
            wb.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportCustomerPerformance(List<CustomerPerformanceResponse> data) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Customer Performance");
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle moneyStyle = createMoneyStyle(wb);

            int row = 0;
            Row header = sheet.createRow(row++);
            String[] cols = {"Customer", "Total Sales", "Total Paid", "Outstanding", "Profit", "Expenses", "Net Profit"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            for (CustomerPerformanceResponse cp : data) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(cp.getCustomer());
                setMoney(r, 1, cp.getTotalSales(), moneyStyle);
                setMoney(r, 2, cp.getTotalPaid(), moneyStyle);
                setMoney(r, 3, cp.getOutstanding(), moneyStyle);
                setMoney(r, 4, cp.getTotalProfit(), moneyStyle);
                setMoney(r, 5, cp.getTotalExpenses(), moneyStyle);
                setMoney(r, 6, cp.getNetProfit(), moneyStyle);
            }

            autoSizeColumns(sheet, cols.length);
            wb.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportProductProfitability(List<ProductProfitResponse> data) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Product Profitability");
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle moneyStyle = createMoneyStyle(wb);

            int row = 0;
            Row header = sheet.createRow(row++);
            String[] cols = {"Product", "Unit", "Qty Sold", "Revenue", "Profit"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            for (ProductProfitResponse pp : data) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(pp.getProduct());
                r.createCell(1).setCellValue(pp.getUnit());
                setMoney(r, 2, pp.getTotalQuantity(), moneyStyle);
                setMoney(r, 3, pp.getTotalRevenue(), moneyStyle);
                setMoney(r, 4, pp.getTotalProfit(), moneyStyle);
            }

            autoSizeColumns(sheet, cols.length);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ===== Helpers =====

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createMoneyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    private void setMoney(Row row, int col, BigDecimal value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value != null ? value.doubleValue() : 0);
        c.setCellStyle(style);
    }

    private int addTitle(Sheet sheet, int row, String title, CellStyle style) {
        Row r = sheet.createRow(row);
        Cell c = r.createCell(0);
        c.setCellValue(title);
        c.setCellStyle(style);
        return row + 1;
    }

    private String fmt(BigDecimal val) {
        return val != null ? val.toPlainString() : "0.00";
    }

    private void autoSizeColumns(Sheet sheet, int colCount) {
        for (int i = 0; i < colCount; i++) sheet.autoSizeColumn(i);
    }
}
