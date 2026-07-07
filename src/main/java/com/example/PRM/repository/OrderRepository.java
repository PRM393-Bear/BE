package com.example.PRM.repository;

import com.example.PRM.entity.Order;
import com.example.PRM.entity.User;
import com.example.PRM.status_enum.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<Order> findBySeller(User seller);
    List<Order> findByStatusAndUpdatedAtBefore(OrderStatus status, LocalDateTime time);
    List<Order> findByBuyerAndStatusOrderByCreatedAtDesc(User buyer, OrderStatus status);

}
