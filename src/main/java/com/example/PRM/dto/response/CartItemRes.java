package com.example.PRM.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CartItemRes {
    private UUID cartItemId;
    private UUID productId;
    private String productName;
    private BigDecimal price;
}
