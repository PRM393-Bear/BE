package com.example.PRM.controller;

import com.example.PRM.dto.request.CategoryReq;
import com.example.PRM.dto.response.ApiResponse;
import com.example.PRM.dto.response.CategoryRes;
import com.example.PRM.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<CategoryRes>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<CategoryRes> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> createCategory(@RequestBody CategoryReq request) {
        categoryService.createCategory(request);
        return ResponseEntity.ok(new ApiResponse(200, "New category was created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> updateCategory(@PathVariable UUID id, @RequestBody CategoryReq request) {
        categoryService.updateCategory(id, request);
        return ResponseEntity.ok(new ApiResponse(200, "Category was updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse(200, "Category was deleted"));
    }
}
