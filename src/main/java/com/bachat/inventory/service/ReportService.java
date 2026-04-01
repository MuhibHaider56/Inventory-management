package com.bachat.inventory.service;

import com.bachat.inventory.domain.OrderPayment;
import com.bachat.inventory.domain.SalesOrder;
import com.bachat.inventory.domain.SalesReturn;
import com.bachat.inventory.dto.*;
import com.bachat.inventory.exception.ResourceNotFoundException;
import com.bachat.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SalesOrderRepository orderRepository;
    private final ExpenseRepository expenseRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final SalesReturnRepository salesReturnRepository;
    private final CustomerRepository customerRepository;

    // ==================== EXISTING ENDPOINTS (kept) ====================

    public SalesSummaryResponse getSalesSummary(LocalDateTime start, LocalDateTime end) {
        BigDecimal totalSales = orderRepository.sumTotalAmountBetween(start, end);
        BigDecimal totalProfit = orderRepository.sumTotalProfitBetween(start, end);
        BigDecimal totalPaid = orderRepository.sumAmountPaidBetween(start, end);
        BigDecimal totalExpenses = expenseRepository.sumAmountBetween(start.toLocalDate(), end.toLocalDate());

        SalesSummaryResponse r = new SalesSummaryResponse();
        r.setTotalSales(totalSales);
        r.setTotalProfit(totalProfit);
        r.setTotalExpenses(totalExpenses);
        r.setNetProfit(totalProfit.subtract(totalExpenses));
        r.setTotalPaid(totalPaid);
        r.setTotalOutstanding(totalSales.subtract(totalPaid));
        return r;
    }

    public SalesSummaryResponse getTodaySummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);
        return getSalesSummary(start, end);
    }

    public List<OverdueResponse> getOverdue() {
        LocalDate today = LocalDate.now();
        return orderRepository.findOverdueOrders(today).stream().map(o -> {
            OverdueResponse r = new OverdueResponse();
            r.setOrderId(o.getId());
            r.setCustomer(o.getCustomer().getName());
            r.setBalance(o.getTotalAmount().subtract(o.getAmountPaid()));
            r.setDueDate(o.getPaymentDueDate());
            boolean overdue = o.getPaymentDueDate() != null && o.getPaymentDueDate().isBefore(today);
            r.setDaysOverdue(overdue ? ChronoUnit.DAYS.between(o.getPaymentDueDate(), today) : 0);
            r.setStatus(overdue ? "OVERDUE" : "PARTIALLY_PAID");
            return r;
        }).toList();
    }

    public List<CustomerPerformanceResponse> getCustomerPerformance(LocalDate start, LocalDate end) {
        List<Object[]> rows;
        if (start != null && end != null) {
            rows = orderRepository.customerPerformanceBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        } else {
            rows = orderRepository.customerPerformance();
        }
        return rows.stream().map(row -> {
            CustomerPerformanceResponse r = new CustomerPerformanceResponse();
            BigDecimal sales = (BigDecimal) row[2];
            BigDecimal paid = (BigDecimal) row[3];
            BigDecimal profit = (BigDecimal) row[4];
            BigDecimal expenses = (BigDecimal) row[5];
            r.setCustomerId((Long) row[0]);
            r.setCustomer((String) row[1]);
            r.setTotalSales(sales);
            r.setTotalPaid(paid);
            r.setOutstanding(sales.subtract(paid));
            r.setTotalProfit(profit);
            r.setTotalExpenses(expenses);
            r.setNetProfit(profit.subtract(expenses));
            return r;
        }).toList();
    }

    public List<ProductProfitResponse> getProductProfitability(LocalDate start, LocalDate end) {
        List<Object[]> rows;
        if (start != null && end != null) {
            rows = orderRepository.productProfitabilityBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        } else {
            rows = orderRepository.productProfitability();
        }
        return rows.stream().map(row -> {
            ProductProfitResponse r = new ProductProfitResponse();
            r.setProductId((Long) row[0]);
            r.setProduct((String) row[1]);
            r.setUnit((String) row[2]);
            r.setTotalQuantity((BigDecimal) row[3]);
            r.setTotalRevenue((BigDecimal) row[4]);
            r.setTotalProfit((BigDecimal) row[5]);
            return r;
        }).toList();
    }

    public List<DailyCollectionResponse> getDailyCollections(LocalDate start, LocalDate end) {
        LocalDate from = start != null ? start : LocalDate.now();
        LocalDate to = end != null ? end : LocalDate.now();
        return orderRepository.dailyCollections(from, to).stream().map(row -> {
            DailyCollectionResponse r = new DailyCollectionResponse();
            r.setDate((LocalDate) row[0]);
            r.setTotalCollected((BigDecimal) row[1]);
            r.setTransactionCount((long) row[2]);
            r.setOrderCount((long) row[3]);
            return r;
        }).toList();
    }

    // ==================== NEW: Dashboard ====================

    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.atTime(23, 59, 59);

        DashboardResponse d = new DashboardResponse();

        // Today's numbers
        SalesSummaryResponse todaySummary = getSalesSummary(dayStart, dayEnd);
        d.setTodaySales(todaySummary.getTotalSales());
        d.setTodayProfit(todaySummary.getNetProfit());
        d.setTodayExpenses(todaySummary.getTotalExpenses());
        d.setTodayCollections(todaySummary.getTotalPaid());
        d.setTodayOrderCount(orderRepository.countOrdersBetween(dayStart, dayEnd));

        // Overdue
        List<OverdueResponse> overdue = getOverdue();
        d.setOverdueCount(overdue.size());
        d.setTotalOverdueAmount(overdue.stream()
                .map(OverdueResponse::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Low stock (threshold = 10)
        d.setLowStockCount(inventoryRepository.countLowStock(BigDecimal.TEN));

        // Total outstanding
        d.setTotalOutstanding(orderRepository.sumTotalOutstanding());

        // Recent 5 orders
        List<SalesOrder> recent = orderRepository.findRecentOrders(PageRequest.of(0, 5));
        d.setRecentOrders(recent.stream().map(o -> {
            OrderResponse r = new OrderResponse();
            r.setId(o.getId());
            r.setInvoiceNumber(o.getInvoiceNumber());
            r.setCustomerId(o.getCustomer().getId());
            r.setCustomerName(o.getCustomer().getName());
            r.setOrderDate(o.getOrderDate());
            r.setStatus(o.getStatus());
            r.setTotalAmount(o.getTotalAmount());
            r.setTotalProfit(o.getTotalProfit());
            return r;
        }).toList());

        return d;
    }

    // ==================== NEW: Period Aggregation ====================

    public List<PeriodSummaryResponse> getMonthlyTrend(LocalDate start, LocalDate end) {
        return aggregate("%Y-%m", start, end);
    }

    public List<PeriodSummaryResponse> getWeeklyTrend(LocalDate start, LocalDate end) {
        return aggregate("%x-W%v", start, end);
    }

    public List<PeriodSummaryResponse> getYearlyTrend(LocalDate start, LocalDate end) {
        return aggregate("%Y", start, end);
    }

    private List<PeriodSummaryResponse> aggregate(String mysqlDateFormat, LocalDate start, LocalDate end) {
        LocalDateTime s = start.atStartOfDay();
        LocalDateTime e = end.plusDays(1).atStartOfDay();

        List<Object[]> rows = orderRepository.aggregateByPeriod(mysqlDateFormat, s, e);

        return rows.stream().map(row -> {
            PeriodSummaryResponse r = new PeriodSummaryResponse();
            r.setPeriod((String) row[0]);
            BigDecimal sales = (BigDecimal) row[1];
            BigDecimal profit = (BigDecimal) row[2];
            BigDecimal expenses = (BigDecimal) row[3];
            BigDecimal paid = (BigDecimal) row[4];
            long count = (long) row[5];

            r.setTotalSales(sales);
            r.setTotalProfit(profit);
            r.setTotalExpenses(expenses);
            r.setNetProfit(profit.subtract(expenses));
            r.setTotalPaid(paid);
            r.setTotalOutstanding(sales.subtract(paid));
            r.setOrderCount(count);

            if (sales.compareTo(BigDecimal.ZERO) > 0) {
                r.setProfitMarginPercent(
                        profit.subtract(expenses)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(sales, 2, RoundingMode.HALF_UP));
            } else {
                r.setProfitMarginPercent(BigDecimal.ZERO);
            }
            return r;
        }).toList();
    }

    // ==================== NEW: Customer Ledger ====================

    public CustomerLedgerResponse getCustomerLedger(Long customerId) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + customerId));

        List<SalesOrder> orders = orderRepository.findByCustomerIdForLedger(customerId);

        List<LedgerEntryResponse> entries = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (SalesOrder order : orders) {
            // Order = debit
            LedgerEntryResponse orderEntry = new LedgerEntryResponse();
            orderEntry.setDate(order.getOrderDate());
            orderEntry.setType("ORDER");
            orderEntry.setReference(order.getInvoiceNumber() != null ? order.getInvoiceNumber() : "ORD-" + order.getId());
            orderEntry.setDescription("Order placed");
            orderEntry.setDebit(order.getTotalAmount());
            orderEntry.setCredit(BigDecimal.ZERO);
            entries.add(orderEntry);
            totalDebit = totalDebit.add(order.getTotalAmount());

            // Payments = credit
            List<OrderPayment> payments = orderPaymentRepository.findByOrderIdOrderByPaymentDateAsc(order.getId());
            for (OrderPayment p : payments) {
                LedgerEntryResponse payEntry = new LedgerEntryResponse();
                payEntry.setDate(p.getPaymentDate());
                payEntry.setType("PAYMENT");
                payEntry.setReference(p.getMethod() != null ? p.getMethod() : "CASH");
                payEntry.setDescription("Payment received" + (p.getReference() != null ? " - " + p.getReference() : ""));
                payEntry.setDebit(BigDecimal.ZERO);
                payEntry.setCredit(p.getAmount());
                entries.add(payEntry);
                totalCredit = totalCredit.add(p.getAmount());
            }

            // Returns = credit
            List<SalesReturn> returns = salesReturnRepository.findByOrderIdOrderByReturnDateDesc(order.getId());
            for (SalesReturn ret : returns) {
                LedgerEntryResponse retEntry = new LedgerEntryResponse();
                retEntry.setDate(ret.getReturnDate());
                retEntry.setType("RETURN");
                retEntry.setReference("RET-" + ret.getId());
                retEntry.setDescription("Return: " + (ret.getReason() != null ? ret.getReason() : "Items returned"));
                retEntry.setDebit(BigDecimal.ZERO);
                retEntry.setCredit(ret.getRefundAmount());
                entries.add(retEntry);
                totalCredit = totalCredit.add(ret.getRefundAmount());
            }
        }

        // Sort by date
        entries.sort(Comparator.comparing(LedgerEntryResponse::getDate));

        // Calculate running balance
        BigDecimal runningBalance = BigDecimal.ZERO;
        for (LedgerEntryResponse entry : entries) {
            runningBalance = runningBalance.add(entry.getDebit()).subtract(entry.getCredit());
            entry.setBalance(runningBalance);
        }

        CustomerLedgerResponse response = new CustomerLedgerResponse();
        response.setCustomerId(customer.getId());
        response.setCustomerName(customer.getName());
        response.setPhone(customer.getPhone());
        response.setTotalDebit(totalDebit);
        response.setTotalCredit(totalCredit);
        response.setClosingBalance(totalDebit.subtract(totalCredit));
        response.setEntries(entries);
        return response;
    }
}
