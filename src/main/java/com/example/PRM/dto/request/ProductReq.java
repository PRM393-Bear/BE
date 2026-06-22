package com.example.PRM.dto.request;


import com.example.PRM.status_enum.ProductStatus;
import com.example.PRM.status_enum.ProductType;

import lombok.Data;

import java.util.List;

@Data
public class ProductReq {
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
    private Short lifecycleGeneration;
    private ProductStatus status;
}