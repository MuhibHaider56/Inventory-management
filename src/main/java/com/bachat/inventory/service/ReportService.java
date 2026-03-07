package com.bachat.inventory.service;

import com.bachat.inventory.dto.*;
import com.bachat.inventory.repository.ExpenseRepository;
import com.bachat.inventory.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SalesOrderRepository orderRepository;
    private final ExpenseRepository expenseRepository;
    public SalesSummaryResponse getSalesSummary(
            LocalDateTime start,
            LocalDateTime end) {

        BigDecimal totalSales = orderRepository.sumTotalAmountBetween(start, end);
        BigDecimal totalProfit = orderRepository.sumTotalProfitBetween(start, end);
        BigDecimal totalPaid = orderRepository.sumAmountPaidBetween(start, end);

        BigDecimal totalExpenses =
                expenseRepository.sumAmountBetween(
                        start.toLocalDate(),
                        end.toLocalDate()
                );

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

        BigDecimal totalSales = orderRepository.sumTotalAmountBetween(start, end);
        BigDecimal totalProfit = orderRepository.sumTotalProfitBetween(start, end);
        BigDecimal totalPaid = orderRepository.sumAmountPaidBetween(start, end);

        BigDecimal totalExpenses =
                expenseRepository.sumAmountBetween(today, today);

        SalesSummaryResponse r = new SalesSummaryResponse();
        r.setTotalSales(totalSales);
        r.setTotalProfit(totalProfit);
        r.setTotalExpenses(totalExpenses);
        r.setNetProfit(totalProfit.subtract(totalExpenses));
        r.setTotalPaid(totalPaid);
        r.setTotalOutstanding(totalSales.subtract(totalPaid));

        return r;
    }
    public List<OverdueResponse> getOverdue() {

        LocalDate today = LocalDate.now();

        return orderRepository.findOverdueOrders(today)
                .stream()
                .map(o -> {
                    OverdueResponse r = new OverdueResponse();
                    r.setOrderId(o.getId());
                    r.setCustomer(o.getCustomer().getName());
                    r.setBalance(o.getTotalAmount().subtract(o.getAmountPaid()));
                    r.setDueDate(o.getPaymentDueDate());
                    r.setDaysOverdue(
                            ChronoUnit.DAYS.between(o.getPaymentDueDate(), today)
                    );
                    return r;
                })
                .toList();
    }
    public List<CustomerPerformanceResponse> getCustomerPerformance(LocalDate start, LocalDate end) {

        List<Object[]> rows;

        if (start != null && end != null) {
            rows = orderRepository.customerPerformanceBetween(
                    start.atStartOfDay(),
                    end.plusDays(1).atStartOfDay()
            );
        } else {
            rows = orderRepository.customerPerformance();
        }

        return rows.stream()
                .map(row -> {
                    CustomerPerformanceResponse r = new CustomerPerformanceResponse();

                    BigDecimal sales    = (BigDecimal) row[2];
                    BigDecimal paid     = (BigDecimal) row[3];
                    BigDecimal profit   = (BigDecimal) row[4];
                    BigDecimal expenses = (BigDecimal) row[5];

                    r.setCustomerId((Long) row[0]);
                    r.setCustomer((String) row[1]);
                    r.setTotalSales(sales);
                    r.setTotalPaid(paid);
                    r.setOutstanding(sales.subtract(paid));
                    r.setTotalProfit(profit);
                    r.setTotalExpenses(expenses);
                    r.setNetProfit(profit.subtract(expenses)); // real net profit
                    return r;
                })
                .toList();
    }
    public List<ProductProfitResponse> getProductProfitability(LocalDate start, LocalDate end) {

        List<Object[]> rows;

        if (start != null && end != null) {
            rows = orderRepository.productProfitabilityBetween(
                    start.atStartOfDay(),
                    end.plusDays(1).atStartOfDay()
            );
        } else {
            rows = orderRepository.productProfitability();
        }

        return rows.stream()
                .map(row -> {
                    ProductProfitResponse r = new ProductProfitResponse();
                    r.setProductId((Long) row[0]);
                    r.setProduct((String) row[1]);
                    r.setUnit((String) row[2]);
                    r.setTotalQuantity((BigDecimal) row[3]);
                    r.setTotalRevenue((BigDecimal) row[4]);
                    r.setTotalProfit((BigDecimal) row[5]);
                    return r;
                })
                .toList();
    }

    public List<DailyCollectionResponse> getDailyCollections(LocalDate start, LocalDate end) {

        // default to today if no range passed
        LocalDate from = start != null ? start : LocalDate.now();
        LocalDate to   = end   != null ? end   : LocalDate.now();

        return orderRepository.dailyCollections(from, to)
                .stream()
                .map(row -> {
                    DailyCollectionResponse r = new DailyCollectionResponse();
                    r.setDate((LocalDate) row[0]);
                    r.setTotalCollected((BigDecimal) row[1]);
                    r.setTransactionCount((long) row[2]);
                    r.setOrderCount((long) row[3]);
                    return r;
                })
                .toList();
    }

}
