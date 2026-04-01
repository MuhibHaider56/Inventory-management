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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    // Fetch-join customer to avoid N+1 on list
    @Query("select o from SalesOrder o join fetch o.customer where o.deleted = false")
    Page<SalesOrder> findByDeletedFalse(Pageable pageable);

    // Filtered list with fetch-join on customer
    @Query("select o from SalesOrder o join fetch o.customer where o.deleted = false " +
           "and (:customerId is null or o.customer.id = :customerId) " +
           "and (:status is null or o.status = :status) " +
           "and (:paymentStatus is null or o.paymentStatus = :paymentStatus) " +
           "and (:start is null or o.orderDate >= :start) " +
           "and (:end is null or o.orderDate <= :end)")
    Page<SalesOrder> findFiltered(@Param("customerId") Long customerId,
                                  @Param("status") OrderStatus status,
                                  @Param("paymentStatus") com.bachat.inventory.domain.PaymentStatus paymentStatus,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  Pageable pageable);

    // Count query for filtered (Spring needs this for Page when using fetch join)
    @Query("select count(o) from SalesOrder o where o.deleted = false " +
           "and (:customerId is null or o.customer.id = :customerId) " +
           "and (:status is null or o.status = :status) " +
           "and (:paymentStatus is null or o.paymentStatus = :paymentStatus) " +
           "and (:start is null or o.orderDate >= :start) " +
           "and (:end is null or o.orderDate <= :end)")
    long countFiltered(@Param("customerId") Long customerId,
                       @Param("status") OrderStatus status,
                       @Param("paymentStatus") com.bachat.inventory.domain.PaymentStatus paymentStatus,
                       @Param("start") LocalDateTime start,
                       @Param("end") LocalDateTime end);

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
    and (o.paymentDueDate < :today or o.amountPaid > 0)
""")
    List<SalesOrder> findOverdueOrders(LocalDate today);

    @Query("""
    select o.customer.id,
           o.customer.name,
           sum(o.totalAmount),
           sum(o.amountPaid),
           sum(o.totalProfit),
           sum(o.totalExpenses)
    from SalesOrder o
    where o.status <> 'CANCELLED'
    group by o.customer.id, o.customer.name
""")
    List<Object[]> customerPerformance();

    @Query("""
    select o.customer.id,
           o.customer.name,
           sum(o.totalAmount),
           sum(o.amountPaid),
           sum(o.totalProfit),
           sum(o.totalExpenses)
    from SalesOrder o
    where o.status <> 'CANCELLED'
    and o.orderDate between :start and :end
    group by o.customer.id, o.customer.name
""")
    List<Object[]> customerPerformanceBetween(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);
    @Query("""
    select oi.product.id,
           oi.product.name,
           oi.product.unit,
           sum(oi.quantity),
           sum(oi.totalPrice),
           sum(oi.profit)
    from OrderItem oi
    where oi.order.status <> 'CANCELLED'
    group by oi.product.id, oi.product.name, oi.product.unit
    order by sum(oi.profit) desc
""")
    List<Object[]> productProfitability();

    @Query("""
    select oi.product.id,
           oi.product.name,
           oi.product.unit,
           sum(oi.quantity),
           sum(oi.totalPrice),
           sum(oi.profit)
    from OrderItem oi
    where oi.order.status <> 'CANCELLED'
    and oi.order.orderDate between :start and :end
    group by oi.product.id, oi.product.name, oi.product.unit
    order by sum(oi.profit) desc
""")
    List<Object[]> productProfitabilityBetween(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    @Query("""
    select cast(p.paymentDate as localdate),
           sum(p.amount),
           count(p.id),
           count(distinct p.order.id)
    from OrderPayment p
    where cast(p.paymentDate as localdate) between :start and :end
    group by cast(p.paymentDate as localdate)
    order by cast(p.paymentDate as localdate)
""")
    List<Object[]> dailyCollections(@Param("start") LocalDate start,
                                    @Param("end") LocalDate end);

    // Dashboard: count today's orders
    @Query("""
        select count(o) from SalesOrder o
        where o.deleted = false and o.status <> 'CANCELLED'
        and o.orderDate between :start and :end
    """)
    long countOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Dashboard: total outstanding across all active orders
    @Query("""
        select coalesce(sum(o.totalAmount - o.amountPaid), 0)
        from SalesOrder o
        where o.deleted = false and o.status <> 'CANCELLED'
        and o.paymentStatus <> 'PAID'
    """)
    BigDecimal sumTotalOutstanding();

    // Dashboard: recent orders with customer fetch-joined
    @Query("select o from SalesOrder o join fetch o.customer where o.deleted = false order by o.orderDate desc")
    List<SalesOrder> findRecentOrders(Pageable pageable);

    // Aggregation: monthly/weekly/yearly grouped data
    @Query("""
        select function('DATE_FORMAT', o.orderDate, :fmt),
               coalesce(sum(o.totalAmount), 0),
               coalesce(sum(o.totalProfit), 0),
               coalesce(sum(o.totalExpenses), 0),
               coalesce(sum(o.amountPaid), 0),
               count(o)
        from SalesOrder o
        where o.deleted = false and o.status <> 'CANCELLED'
        and o.orderDate between :start and :end
        group by function('DATE_FORMAT', o.orderDate, :fmt)
        order by function('DATE_FORMAT', o.orderDate, :fmt)
    """)
    List<Object[]> aggregateByPeriod(@Param("fmt") String dateFormat,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    // Ledger: all orders for a customer
    @Query("""
        select o from SalesOrder o
        where o.customer.id = :customerId
        and o.deleted = false and o.status <> 'CANCELLED'
        order by o.orderDate asc
    """)
    List<SalesOrder> findByCustomerIdForLedger(@Param("customerId") Long customerId);

    List<SalesOrder> findByDeletedTrueAndDeletedAtBefore(LocalDateTime cutoff);
}
