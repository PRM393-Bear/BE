package com.example.PRM.dto.request;

import com.example.PRM.entity.Product;
import lombok.Data;

@Data
public class ProductFilterReq {

    private String category;
    private Long minPrice;
    private Long maxPrice;
    private Short condition;
    private String size;
    private String color;
    private String location;
    private Product.ProductType type;
    private String sortBy;

}
