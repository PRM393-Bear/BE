package com.example.PRM.service;

import com.example.PRM.dto.request.CreateOrderReq;
import com.example.PRM.dto.response.OrderRes;
import com.example.PRM.entity.Order;
import com.example.PRM.status_enum.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    Order createOrder(UserDetails userDetails, CreateOrderReq req);
    Order confirmOrder(UserDetails userDetails, UUID orderId);
    Order enterShipOrder(UserDetails userDetails, UUID orderId, String trackingCode);
    Order confirmReceived(UserDetails userDetails, UUID orderId, String deliveryPhotoUrl, HttpServletRequest request);
    List<OrderRes> getOrdersByBuyer(UserDetails userDetails);
    List<OrderRes> getOrdersBySeller(UserDetails userDetails);
    Order getOrderById(UserDetails userDetails, UUID orderId);
    List<OrderRes> getOrderHistory(UserDetails userDetails, OrderStatus status);
    void autoProcessCompleteOrder();
    Order updatePickupPhoto(UserDetails userDetails, UUID orderId, String photoUrl);
}
