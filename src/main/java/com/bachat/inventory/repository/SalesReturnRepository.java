package com.bachat.inventory.repository;

import com.bachat.inventory.domain.SalesReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalesReturnRepository extends JpaRepository<SalesReturn, Long> {
    List<SalesReturn> findByOrderIdOrderByReturnDateDesc(Long orderId);
    Page<SalesReturn> findAllByOrderByReturnDateDesc(Pageable pageable);
}
