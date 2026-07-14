package com.example.PRM.mapper;

import com.example.PRM.dto.request.category.CategoryReq;
import com.example.PRM.dto.response.CategoryRes;
import com.example.PRM.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryRes toResponse(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryRes.builder()
                .id(category.getId() != null ? category.getId().toString() : null)
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    public Category toEntity(CategoryReq req) {
        if (req == null) {
            return null;
        }

        return Category.builder()
                .name(req.getName())
                .description(req.getDescription())
                .build();
    }
}
