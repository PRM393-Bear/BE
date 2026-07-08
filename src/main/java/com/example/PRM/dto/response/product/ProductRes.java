package com.example.PRM.dto.response.product;

import com.example.PRM.status_enum.ProductStatus;
import com.example.PRM.status_enum.ProductType;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ProductRes {
    private UUID id;
    private String title;
    private String description;
    private String category;
    private ProductType type;
    private Short condition;
    private Long price;
    private String size;
    private String color;
    private List<String> images;
    private List<String> aiTags;
    private ProductStatus status;
    private Short lifecycleGeneration;
    private OffsetDateTime createdAt;

    private UUID sellerId;
    private String sellerName;
}