package com.bachat.inventory.repository;

import com.bachat.inventory.domain.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    /**
     * Returns [totalQuantity, totalCost] for a product across all RECEIVED purchase orders.
     * Used to calculate weighted average cost.
     */
    @Query("""
        select coalesce(sum(poi.quantity), 0),
               coalesce(sum(poi.totalPrice), 0)
        from PurchaseOrderItem poi
        where poi.product.id = :productId
        and poi.purchaseOrder.status = 'RECEIVED'
    """)
    List<Object[]> sumQuantityAndCostByProduct(@Param("productId") Long productId);

    List<PurchaseOrderItem> findByPurchaseOrderId(Long poId);
}
