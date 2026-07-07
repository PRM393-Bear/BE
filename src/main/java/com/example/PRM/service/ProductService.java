package com.example.PRM.service;

import com.example.PRM.dto.request.ProductFilterReq;
import com.example.PRM.dto.request.ProductReq;
import com.example.PRM.dto.response.ProductRes;
import com.example.PRM.entity.Product;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductRes getProductById(UUID id);
    List<ProductRes> getAllProducts();
    ProductRes createProduct(ProductReq request, HttpServletRequest request1);
    List<ProductRes> search(String category, Long maxPrice);
    List<ProductRes> searchProductByKeyword(String keyword);
    ProductRes updateProduct(UUID id, ProductReq request, HttpServletRequest request1);
    List<ProductRes> getProductsByUserId(UUID userId);
    ProductRes hideProduct(UUID id,HttpServletRequest request1);
    List<ProductRes> filterProducts(ProductFilterReq filter);
    void deleteProduct(UUID id, HttpServletRequest request);
}