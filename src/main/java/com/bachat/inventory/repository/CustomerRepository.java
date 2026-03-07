package com.bachat.inventory.repository;

import com.bachat.inventory.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByDeletedFalse(Pageable pageable);

    @Query("select c from Customer c where c.deleted = false " +
           "and (lower(c.name) like lower(concat('%', :q, '%')) " +
           "or c.phone like concat('%', :q, '%'))")
    Page<Customer> searchByNameOrPhone(@Param("q") String query, Pageable pageable);

    @Query("""
        select coalesce(sum(o.totalAmount) - sum(o.amountPaid), 0)
        from SalesOrder o
        where o.customer.id = :customerId
        and o.status <> 'CANCELLED'
        and o.deleted = false
    """)
    BigDecimal getOutstandingBalance(@Param("customerId") Long customerId);

    List<Customer> findByDeletedTrueAndDeletedAtBefore(LocalDateTime cutoff);
}
