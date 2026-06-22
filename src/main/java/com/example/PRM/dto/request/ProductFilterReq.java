package com.example.PRM.dto.request;

import com.example.PRM.entity.Product;
import com.example.PRM.status_enum.ProductType;
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
    private ProductType type;
    private String sortBy;

}
