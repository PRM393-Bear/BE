package com.example.PRM.service;

import com.example.PRM.dto.request.CreateOrderReq;
import com.example.PRM.entity.Order;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    Order createOrder(UserDetails userDetails, CreateOrderReq req);
    Order confirmOrder(UserDetails userDetails, UUID orderId);
    Order enterShipOrder(UserDetails userDetails, UUID orderId, String trackingCode);
    Order confirmReceived(UserDetails userDetails, UUID orderId);
    
    List<Order> getOrdersByBuyer(UserDetails userDetails);
    List<Order> getOrdersBySeller(UserDetails userDetails);
    Order getOrderById(UserDetails userDetails, UUID orderId);
    
    void autoProcessCompleteOrder();
}
