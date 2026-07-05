package com.example.PRM.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class AddCartItemReq {
    private UUID productId;
}
