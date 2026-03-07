package com.bachat.inventory.repository;

import com.bachat.inventory.domain.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Page<Supplier> findByDeletedFalse(Pageable pageable);

    @Query("select s from Supplier s where s.deleted = false " +
           "and (lower(s.name) like lower(concat('%', :q, '%')) " +
           "or s.phone like concat('%', :q, '%'))")
    Page<Supplier> searchByNameOrPhone(@Param("q") String query, Pageable pageable);
}
