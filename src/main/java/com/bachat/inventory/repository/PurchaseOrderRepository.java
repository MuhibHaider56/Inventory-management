package com.bachat.inventory.repository;

import com.bachat.inventory.domain.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("select po from PurchaseOrder po join fetch po.supplier where po.id = :id")
    Optional<PurchaseOrder> findDetailedById(@Param("id") Long id);

    @Query("select po from PurchaseOrder po join fetch po.supplier")
    Page<PurchaseOrder> findAllWithSupplier(Pageable pageable);

    @Query("select po from PurchaseOrder po join fetch po.supplier where po.supplier.id = :supplierId")
    Page<PurchaseOrder> findBySupplierId(@Param("supplierId") Long supplierId, Pageable pageable);

    boolean existsBySupplier_Id(Long supplierId);
}
