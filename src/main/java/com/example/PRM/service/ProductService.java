package com.example.PRM.service;

import com.example.PRM.dto.request.ProductReq;
import com.example.PRM.dto.response.ProductRes;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductRes getProductById(UUID id);
    List<ProductRes> getAllProducts();
    ProductRes createProduct(ProductReq request);
}