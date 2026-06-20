package com.example.PRM.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem { // OrderItem là những đồ bên trong 1 cái đơn đặt hàng

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Thuộc đơn hàng nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Sản phẩm được mua
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    // Giá tại thời điểm mua
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    // quantity * unitPrice
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

}
