package com.example.PRM.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateOrderReq {
    private UUID productId;
    private Integer quantity;
}
