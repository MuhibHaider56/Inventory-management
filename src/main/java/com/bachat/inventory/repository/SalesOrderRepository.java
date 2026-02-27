package com.bachat.inventory.repository;

import com.bachat.inventory.domain.OrderStatus;
import com.bachat.inventory.domain.SalesOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    boolean existsByCustomer_Id(Long customerId);

    Page<SalesOrder> findAllByOrderDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("select distinct o from SalesOrder o " +
            "join fetch o.customer c " +
            "left join fetch o.items i " +
            "left join fetch i.product p " +
            "where o.id = :id")
    Optional<SalesOrder> findDetailedById(@Param("id") Long id);

    @Query("select coalesce(sum(o.totalAmount), 0) from SalesOrder o " +
            "where o.status = :status and o.orderDate >= :start and o.orderDate < :end")
    BigDecimal sumTotalAmount(@Param("status") OrderStatus status,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    @Query("select coalesce(sum(o.totalProfit), 0) from SalesOrder o " +
            "where o.status = :status and o.orderDate >= :start and o.orderDate < :end")
    BigDecimal sumTotalProfit(@Param("status") OrderStatus status,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from SalesOrder o where o.id = :id")
    Optional<SalesOrder> findByIdForUpdate(@Param("id") Long id);

    @Query("""
    select coalesce(sum(o.totalAmount),0)
    from SalesOrder o
    where o.orderDate between :start and :end
    and o.status <> 'CANCELLED'
""")
    BigDecimal sumTotalAmountBetween(LocalDateTime  start, LocalDateTime  end);

    @Query("""
    select coalesce(sum(o.totalProfit),0)
    from SalesOrder o
    where o.orderDate between :start and :end
    and o.status <> 'CANCELLED'
""")
    BigDecimal sumTotalProfitBetween(LocalDateTime  start, LocalDateTime  end);

    @Query("""
    select coalesce(sum(o.amountPaid),0)
    from SalesOrder o
    where o.orderDate between :start and :end
    and o.status <> 'CANCELLED'
""")
    BigDecimal sumAmountPaidBetween(LocalDateTime  start, LocalDateTime  end);

    @Query("""
    select o from SalesOrder o
    where o.paymentStatus <> 'PAID'
    and o.status <> 'CANCELLED'
    and o.paymentDueDate < :today
""")
    List<SalesOrder> findOverdueOrders(LocalDate today);

    @Query("""
    select o.customer.name,
           sum(o.totalAmount),
           sum(o.amountPaid)
    from SalesOrder o
    where o.status <> 'CANCELLED'
    group by o.customer.name
""")
    List<Object[]> customerPerformance();

    @Query("""
    select oi.product.name,
           sum(oi.quantity),
           sum(oi.profit)
    from OrderItem oi
    group by oi.product.name
    order by sum(oi.profit) desc
""")
    List<Object[]> productProfitability();

    @Query("""
    select date(p.paymentDate), sum(p.amount)
    from OrderPayment p
    group by date(p.paymentDate)
    order by date(p.paymentDate)
""")
    List<Object[]> dailyCollections();
}
