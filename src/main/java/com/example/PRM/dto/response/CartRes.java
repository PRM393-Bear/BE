package com.example.PRM.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Data
public class CartRes {
    private UUID cartId;
    private UUID userId;
    private List<CartItemRes> items;
    private BigDecimal totalPrice;
}
