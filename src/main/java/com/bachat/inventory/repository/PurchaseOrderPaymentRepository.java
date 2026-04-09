package com.bachat.inventory.repository;

import com.bachat.inventory.domain.PurchaseOrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseOrderPaymentRepository extends JpaRepository<PurchaseOrderPayment, Long> {

    @Query("SELECT p FROM PurchaseOrderPayment p WHERE p.purchaseOrder.id = :poId ORDER BY p.paymentDate ASC")
    List<PurchaseOrderPayment> findByPurchaseOrderId(@Param("poId") Long poId);
}
