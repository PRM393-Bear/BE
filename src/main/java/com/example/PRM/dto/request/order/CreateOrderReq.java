package com.example.PRM.dto.request.order;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateOrderReq {
    private UUID productId;
}
