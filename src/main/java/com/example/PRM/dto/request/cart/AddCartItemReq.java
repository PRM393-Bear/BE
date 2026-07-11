package com.example.PRM.dto.request.cart;

import lombok.Data;

import java.util.UUID;

@Data
public class AddCartItemReq {
    private UUID productId;
}
