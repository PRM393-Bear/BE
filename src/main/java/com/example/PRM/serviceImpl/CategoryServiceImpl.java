package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.category.CategoryReq;
import com.example.PRM.dto.response.CategoryRes;
import com.example.PRM.entity.Category;
import com.example.PRM.repository.CategoryRepository;
import com.example.PRM.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryRes> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToRes)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryRes getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return mapToRes(category);
    }

    @Override
    public CategoryRes createCategory(CategoryReq request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category name already exists");
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return mapToRes(categoryRepository.save(category));
    }

    @Override
    public CategoryRes updateCategory(UUID id, CategoryReq request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return mapToRes(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    private CategoryRes mapToRes(Category category) {
        return CategoryRes.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
