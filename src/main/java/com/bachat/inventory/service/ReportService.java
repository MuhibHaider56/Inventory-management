package com.bachat.inventory.service;

import com.bachat.inventory.dto.CustomerPerformanceResponse;
import com.bachat.inventory.dto.OverdueResponse;
import com.bachat.inventory.dto.ProductProfitResponse;
import com.bachat.inventory.dto.SalesSummaryResponse;
import com.bachat.inventory.repository.ExpenseRepository;
import com.bachat.inventory.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

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
    public List<CustomerPerformanceResponse> getCustomerPerformance() {

        return orderRepository.customerPerformance()
                .stream()
                .map(row -> {
                    CustomerPerformanceResponse r = new CustomerPerformanceResponse();
                    BigDecimal sales = (BigDecimal) row[1];
                    BigDecimal paid = (BigDecimal) row[2];

                    r.setCustomer((String) row[0]);
                    r.setTotalSales(sales);
                    r.setTotalPaid(paid);
                    r.setOutstanding(sales.subtract(paid));
                    return r;
                })
                .toList();
    }
    public List<ProductProfitResponse> getProductProfitability() {

        return orderRepository.productProfitability()
                .stream()
                .map(row -> {
                    ProductProfitResponse r = new ProductProfitResponse();
                    r.setProduct((String) row[0]);
                    r.setTotalQuantity((BigDecimal) row[1]);
                    r.setTotalProfit((BigDecimal) row[2]);
                    return r;
                })
                .toList();
    }

    public List<Map<String,Object>> getDailyCollections() {

        return orderRepository.dailyCollections()
                .stream()
                .map(row -> Map.of(
                        "date", row[0],
                        "totalCollected", row[1]
                ))
                .toList();
    }

}
