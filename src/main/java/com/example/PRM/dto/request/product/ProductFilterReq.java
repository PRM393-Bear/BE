package com.example.PRM.dto.request.product;

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
