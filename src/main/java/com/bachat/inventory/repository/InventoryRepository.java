package com.bachat.inventory.repository;

import com.bachat.inventory.domain.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProduct_Id(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.product.id = :productId")
    Optional<Inventory> findByProductIdForUpdate(@Param("productId") Long productId);

    // Fetch-join product to avoid N+1 on list
    @Query("select i from Inventory i join fetch i.product")
    List<Inventory> findAllWithProduct();

    // Fetch-join product for low-stock query
    @Query("select i from Inventory i join fetch i.product where i.quantityAvailable < :threshold")
    List<Inventory> findLowStockWithProduct(@Param("threshold") BigDecimal threshold);

    @Query("select count(i) from Inventory i where i.quantityAvailable < :threshold")
    long countLowStock(@Param("threshold") BigDecimal threshold);
}
