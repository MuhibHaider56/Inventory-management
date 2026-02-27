package com.bachat.inventory.repository;

import com.bachat.inventory.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    boolean existsByProduct_Id(Long productId);
}
