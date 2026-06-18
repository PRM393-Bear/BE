package com.example.PRM.service;

import com.example.PRM.dto.request.ProductReq;
import com.example.PRM.dto.response.ProductRes;
import com.example.PRM.entity.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductRes getProductById(UUID id);
    List<ProductRes> getAllProducts();
    ProductRes createProduct(ProductReq request);
    List<ProductRes> search(String category, Long maxPrice);

}