package com.example.PRM.controller;

import com.example.PRM.dto.request.ProductReq;
import com.example.PRM.dto.response.ProductRes;
import com.example.PRM.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductRes>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductRes> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductRes> createProduct(@RequestBody ProductReq request) {
        ProductRes created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}