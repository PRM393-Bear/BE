package com.example.PRM.dto.request.product;


import com.example.PRM.status_enum.ProductType;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ProductReq {
    private String title;
    private String description;
    private UUID categoryId;
    private ProductType type;
    private Short condition;
    private Long price;
    private String size;
    private String color;
    private List<String> images;
    private List<String> aiTags;
    private Short lifecycleGeneration;
    private String brand;
}