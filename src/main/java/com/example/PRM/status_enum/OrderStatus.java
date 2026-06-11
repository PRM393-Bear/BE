package com.example.PRM.status_enum;

public enum OrderStatus {
    PENDING,      // Chờ thanh toán
    PAID,         // Đã thanh toán
    PROCESSING,   // Đang xử lý
    SHIPPING,     // Đang giao
    DELIVERED,    // Đã giao
    CANCELLED     // Đã hủy
}
