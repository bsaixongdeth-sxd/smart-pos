package com.pos.order.repository;

import com.pos.order.entity.Order;
import com.pos.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    List<Order> findByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime from, LocalDateTime to);

    List<Order> findByCashierIdAndCreatedAtBetween(UUID cashierId, LocalDateTime from, LocalDateTime to);

    List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
