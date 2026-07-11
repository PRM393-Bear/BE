package com.example.PRM.service;

import com.example.PRM.dto.request.product.ProductFilterReq;
import com.example.PRM.dto.request.product.ProductReq;
import com.example.PRM.dto.response.product.ProductRes;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductRes getProductById(UUID id);
    List<ProductRes> getAllProducts();
    ProductRes createProduct(ProductReq request);
    List<ProductRes> search(String category, Long maxPrice);
    List<ProductRes> searchProductByKeyword(String keyword);
    ProductRes updateProduct(UUID id, ProductReq request);
    List<ProductRes> getProductsByUserId(UUID userId);
    ProductRes hideProduct(UUID id);
    List<ProductRes> filterProducts(ProductFilterReq filter);
    ProductRes deleteProduct(UUID id);
    List<ProductRes> getProductPendingStatus();
    ProductRes approveProduct(UUID id);
    ProductRes rejectProduct(UUID id, String rejectReason);
    List<ProductRes> getMyRejectedProducts(UserDetails userDetails);
    ProductRes unhideProduct(UUID id);
}