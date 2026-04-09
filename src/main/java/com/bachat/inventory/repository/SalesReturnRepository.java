package com.bachat.inventory.repository;

import com.bachat.inventory.domain.SalesReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SalesReturnRepository extends JpaRepository<SalesReturn, Long> {
    List<SalesReturn> findByOrderIdOrderByReturnDateDesc(Long orderId);
    Page<SalesReturn> findAllByOrderByReturnDateDesc(Pageable pageable);

    @Query("select coalesce(sum(ri.quantity), 0) from SalesReturn sr join sr.items ri where ri.orderItem.id = :orderItemId")
    BigDecimal sumReturnedQtyByOrderItemId(@Param("orderItemId") Long orderItemId);
}
