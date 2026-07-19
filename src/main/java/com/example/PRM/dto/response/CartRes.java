package com.example.PRM.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartRes {
    private UUID cartId;
    private UUID userId;
    private List<CartItemRes> items;
    private BigDecimal totalPrice;
}
