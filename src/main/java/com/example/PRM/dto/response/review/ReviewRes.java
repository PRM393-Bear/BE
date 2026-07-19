package com.example.PRM.dto.response.review;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewRes {
    private UUID id;
    private UUID orderId;
    private UUID reviewerId;
    private String reviewerName;
    private Short rating;
    private String comment;
    private OffsetDateTime createdAt;
    private String productTitle;
}
