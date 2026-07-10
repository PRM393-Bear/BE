package com.example.PRM.dto.response;

import com.example.PRM.status_enum.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OrderRes {
    private UUID id;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String trackingCode;
    private String pickupPhotoUrl;
    private String deliveryPhotoUrl;

    private UUID buyerId;
    private String buyerName;
    private UUID sellerId;
    private String sellerName;

    private List<OrderItemRes> items;
}
