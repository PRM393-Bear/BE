package com.example.PRM.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewReq {
    private UUID orderId;
    private Short rating;
    private String comment;
}