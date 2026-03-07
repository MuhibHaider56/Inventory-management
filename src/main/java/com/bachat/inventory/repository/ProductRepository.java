package com.bachat.inventory.repository;

import com.bachat.inventory.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByDeletedFalse(Pageable pageable);

    @Query("select p from Product p where p.deleted = false " +
           "and lower(p.name) like lower(concat('%', :q, '%'))")
    Page<Product> searchByName(@Param("q") String query, Pageable pageable);

    List<Product> findByDeletedTrueAndDeletedAtBefore(LocalDateTime cutoff);
}
