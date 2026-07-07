package com.example.PRM.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderItemRes {
    private UUID id;
    private UUID productId;
    private String productTitle;
    private String productImage;
    private BigDecimal unitPrice;
}
