package com.bachat.inventory.service;

import com.bachat.inventory.dto.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final String CURRENCY = "PKR ";

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font BODY_BOLD = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL);
    private static final Font COMPANY_FONT = new Font(Font.HELVETICA, 12, Font.BOLD);

    @Value("${app.company.name:Your Company Name}")
    private String companyName;

    @Value("${app.company.address:123 Main Street, Karachi}")
    private String companyAddress;

    @Value("${app.company.phone:0300-1234567}")
    private String companyPhone;

    @Value("${app.company.ntn:NTN: 0000000-0}")
    private String companyNtn;

    // ==================== Invoice PDF ====================

    public byte[] generateInvoicePdf(InvoiceResponse invoice) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Company header
            addCompanyHeader(doc);
            doc.add(new Paragraph("\n"));

            // Invoice title
            Paragraph title = new Paragraph("INVOICE", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph("\n"));

            // Invoice details table (2 columns)
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setWidths(new float[]{1, 1});

            // Left: customer info
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(0);
            leftCell.addElement(new Paragraph("Bill To:", HEADER_FONT));
            leftCell.addElement(new Paragraph(invoice.getCustomerName(), BODY_BOLD));
            detailsTable.addCell(leftCell);

            // Right: invoice info
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(0);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            String invNum = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "ORD-" + invoice.getOrderId();
            rightCell.addElement(new Paragraph("Invoice #: " + invNum, BODY_BOLD));
            rightCell.addElement(new Paragraph("Date: " + invoice.getOrderDate().format(DATETIME_FMT), BODY_FONT));
            if (invoice.getPaymentDueDate() != null) {
                rightCell.addElement(new Paragraph("Due Date: " + invoice.getPaymentDueDate().format(DATE_FMT), BODY_FONT));
            }
            rightCell.addElement(new Paragraph("Status: " + invoice.getPaymentStatus(), BODY_FONT));
            detailsTable.addCell(rightCell);
            doc.add(detailsTable);
            doc.add(new Paragraph("\n"));

            // Line items table
            PdfPTable itemsTable = new PdfPTable(5);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{3, 1.5f, 1, 1.5f, 1.5f});

            addTableHeader(itemsTable, new String[]{"Product", "Qty", "Unit", "Rate", "Amount"});

            for (InvoiceItem item : invoice.getItems()) {
                addBodyCell(itemsTable, item.getProduct());
                addBodyCell(itemsTable, item.getQuantity().toPlainString());
                addBodyCell(itemsTable, item.getUnit());
                addBodyCellRight(itemsTable, CURRENCY + fmt(item.getSellingPrice()));
                addBodyCellRight(itemsTable, CURRENCY + fmt(item.getTotalPrice()));
            }
            doc.add(itemsTable);

            // Totals
            doc.add(new Paragraph("\n"));
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(50);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            addTotalRow(totalsTable, "Subtotal:", CURRENCY + fmt(invoice.getTotalAmount()));
            if (invoice.getTotalExpenses() != null && invoice.getTotalExpenses().compareTo(BigDecimal.ZERO) > 0) {
                addTotalRow(totalsTable, "Expenses:", CURRENCY + fmt(invoice.getTotalExpenses()));
            }
            addTotalRow(totalsTable, "Amount Paid:", CURRENCY + fmt(invoice.getAmountPaid()));
            addTotalRowBold(totalsTable, "Balance Due:", CURRENCY + fmt(invoice.getBalanceDue()));
            doc.add(totalsTable);

            // Payments section
            if (invoice.getPayments() != null && !invoice.getPayments().isEmpty()) {
                doc.add(new Paragraph("\n"));
                doc.add(new Paragraph("Payment History", HEADER_FONT));
                PdfPTable payTable = new PdfPTable(3);
                payTable.setWidthPercentage(70);
                addTableHeader(payTable, new String[]{"Date", "Method", "Amount"});
                for (InvoicePayment p : invoice.getPayments()) {
                    addBodyCell(payTable, p.getPaymentDate().format(DATETIME_FMT));
                    addBodyCell(payTable, p.getMethod() != null ? p.getMethod() : "—");
                    addBodyCellRight(payTable, CURRENCY + fmt(p.getAmount()));
                }
                doc.add(payTable);
            }

            doc.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph("Thank you for your business!", SMALL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        }
    }

    // ==================== Customer Ledger PDF ====================

    public byte[] generateLedgerPdf(CustomerLedgerResponse ledger) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 30, 30); // landscape
            PdfWriter.getInstance(doc, out);
            doc.open();

            addCompanyHeader(doc);
            doc.add(new Paragraph("\n"));

            Paragraph title = new Paragraph("ACCOUNT STATEMENT", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            doc.add(new Paragraph("Customer: " + ledger.getCustomerName()
                    + (ledger.getPhone() != null ? "  |  Phone: " + ledger.getPhone() : ""), BODY_BOLD));
            doc.add(new Paragraph("\n"));

            // Ledger table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 1.2f, 2, 3, 1.5f, 1.5f, 1.5f});

            addTableHeader(table, new String[]{"Date", "Type", "Reference", "Description", "Debit", "Credit", "Balance"});

            for (LedgerEntryResponse entry : ledger.getEntries()) {
                addBodyCell(table, entry.getDate().format(DATETIME_FMT));
                addBodyCell(table, entry.getType());
                addBodyCell(table, entry.getReference());
                addBodyCell(table, entry.getDescription());
                addBodyCellRight(table, entry.getDebit().compareTo(BigDecimal.ZERO) > 0 ? CURRENCY + fmt(entry.getDebit()) : "—");
                addBodyCellRight(table, entry.getCredit().compareTo(BigDecimal.ZERO) > 0 ? CURRENCY + fmt(entry.getCredit()) : "—");
                addBodyCellRight(table, CURRENCY + fmt(entry.getBalance()));
            }

            doc.add(table);
            doc.add(new Paragraph("\n"));

            // Summary
            PdfPTable summary = new PdfPTable(2);
            summary.setWidthPercentage(40);
            summary.setHorizontalAlignment(Element.ALIGN_RIGHT);
            addTotalRow(summary, "Total Debit:", CURRENCY + fmt(ledger.getTotalDebit()));
            addTotalRow(summary, "Total Credit:", CURRENCY + fmt(ledger.getTotalCredit()));
            addTotalRowBold(summary, "Closing Balance:", CURRENCY + fmt(ledger.getClosingBalance()));
            doc.add(summary);

            doc.close();
            return out.toByteArray();
        }
    }

    // ==================== Helpers ====================

    private void addCompanyHeader(Document doc) throws DocumentException {
        Paragraph name = new Paragraph(companyName, COMPANY_FONT);
        name.setAlignment(Element.ALIGN_CENTER);
        doc.add(name);

        Paragraph details = new Paragraph(companyAddress + "  |  " + companyPhone + "  |  " + companyNtn, SMALL_FONT);
        details.setAlignment(Element.ALIGN_CENTER);
        doc.add(details);

        // Line separator
        doc.add(new Paragraph("________________________________________________________________________________", SMALL_FONT));
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(new Color(230, 230, 230));
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "—", BODY_FONT));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void addBodyCellRight(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BODY_FONT));
        cell.setPadding(4);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BODY_FONT));
        labelCell.setBorder(0);
        labelCell.setPadding(3);
        table.addCell(labelCell);

        PdfPCell valCell = new PdfPCell(new Phrase(value, BODY_FONT));
        valCell.setBorder(0);
        valCell.setPadding(3);
        valCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valCell);
    }

    private void addTotalRowBold(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BODY_BOLD));
        labelCell.setBorderWidthTop(1);
        labelCell.setBorderWidthBottom(0);
        labelCell.setBorderWidthLeft(0);
        labelCell.setBorderWidthRight(0);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valCell = new PdfPCell(new Phrase(value, BODY_BOLD));
        valCell.setBorderWidthTop(1);
        valCell.setBorderWidthBottom(0);
        valCell.setBorderWidthLeft(0);
        valCell.setBorderWidthRight(0);
        valCell.setPadding(4);
        valCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valCell);
    }

    private String fmt(BigDecimal val) {
        if (val == null) return "0.00";
        return String.format("%,.2f", val);
    }
}
