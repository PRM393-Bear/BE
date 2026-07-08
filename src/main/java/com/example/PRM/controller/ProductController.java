package com.example.PRM.controller;

import com.example.PRM.dto.request.ProductFilterReq;
import com.example.PRM.dto.request.ProductReq;
import com.example.PRM.dto.response.product.ProductRes;
import com.example.PRM.service.AuditLogService;
import com.example.PRM.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<ProductRes>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductRes> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductRes> createProduct(@RequestBody ProductReq request,
                                                    HttpServletRequest request1) {
        ProductRes created = productService.createProduct(request);
        auditLogService.log("CREATE_PRODUCT",
                created.getTitle(),
                created.getId().toString(),
                "User reset password successfully",
                "SUCCESS",
                created.getSellerId(),
                created.getSellerName(),
                request1
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/search-by-keyword")
    public ResponseEntity<List<ProductRes>> searchByKeyword(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(productService.searchProductByKeyword(keyword));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductRes> updateProduct(
            @PathVariable UUID id,
            @RequestBody ProductReq request,
            HttpServletRequest request1
    ) {
        ProductRes updated = productService.updateProduct(id, request);
        auditLogService.log("UPDATE_PRODUCT",
                updated.getTitle(),
                updated.getId().toString(),
                "Seller update product successfully",
                "SUCCESS",
                updated.getSellerId(),
                updated.getSellerName(),
                request1
        );
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/user/product")
    public ResponseEntity<List<ProductRes>> getProductsByUserId(@RequestParam("userId") UUID userId) {
        List<ProductRes> products = productService.getProductsByUserId(userId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/hide")
    public ResponseEntity<ProductRes> hideProduct(@RequestParam("productId") UUID id,
                                                  HttpServletRequest request1) {
        ProductRes hiddenProduct = productService.hideProduct(id);
        auditLogService.log("HIDE_PRODUCT",
                hiddenProduct.getTitle(),
                hiddenProduct.getId().toString(),
                "Seller hide product successfully",
                "SUCCESS",
                hiddenProduct.getSellerId(),
                hiddenProduct.getSellerName(),
                request1
        );
        return ResponseEntity.ok(hiddenProduct);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ProductRes>> filterProducts(ProductFilterReq filter) {
        List<ProductRes> results = productService.filterProducts(filter);
        return ResponseEntity.ok(results);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteProduct(@RequestParam("productId") UUID id, HttpServletRequest request1) {
        ProductRes deleted = productService.deleteProduct(id);
        auditLogService.log("DELETE_PRODUCT",
                deleted.getTitle(),
                deleted.getId().toString(),
                "User delete product successfully",
                "SUCCESS",
                deleted.getSellerId(),
                deleted.getSellerName(),
                request1
        );
        return ResponseEntity.ok("Product deleted successfully");
    }
}