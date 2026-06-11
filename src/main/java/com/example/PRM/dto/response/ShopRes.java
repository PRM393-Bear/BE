package com.example.PRM.dto.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopRes {
    @Column(nullable = false, length = 150)
    private String shopName;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 1000)
    private String description;

    @Column(length = 20)
    private String phone;

    @Column(precision = 12, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 12, scale = 8)
    private BigDecimal longitude;
}
