package com.example.PRM.dto.request.review;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Data
public class ReviewReq {
    @NotNull(message = "Order ID không được để trống")
    private UUID orderId;

    @NotNull(message = "Rating không được để trống")
    @Min(value = 1, message = "Rating tối thiểu là 1")
    @Max(value = 5, message = "Rating tối đa là 5")
    private Short rating;

    private String comment;
}