package com.example.PRM.service;

import com.example.PRM.dto.request.CategoryReq;
import com.example.PRM.dto.response.CategoryRes;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<CategoryRes> getAllCategories();
    CategoryRes getCategoryById(UUID id);
    CategoryRes createCategory(CategoryReq request);
    CategoryRes updateCategory(UUID id, CategoryReq request);
    void deleteCategory(UUID id);
}
