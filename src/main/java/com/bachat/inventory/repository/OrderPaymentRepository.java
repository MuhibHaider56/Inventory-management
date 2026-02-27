package com.bachat.inventory.repository;

import com.bachat.inventory.domain.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {

    List<OrderPayment> findByOrderIdOrderByPaymentDateAsc(Long orderId);

    List<OrderPayment> findByPaymentDateBetween(Instant start, Instant end);
}