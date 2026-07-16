package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.category.CategoryReq;
import com.example.PRM.dto.response.CategoryRes;
import com.example.PRM.entity.Category;
import com.example.PRM.mapper.CategoryMapper;
import com.example.PRM.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    private Category category;
    private CategoryReq categoryReq;
    private CategoryRes categoryRes;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        category = new Category();
        category.setId(categoryId);
        category.setName("Electronics");
        category.setDescription("Electronic items");

        categoryReq = new CategoryReq();
        categoryReq.setName("Electronics");
        categoryReq.setDescription("Electronic items");

        categoryRes = CategoryRes.builder().build();
        categoryRes.setId(category.getId().toString());
        categoryRes.setName("Electronics");
        categoryRes.setDescription("Electronic items");
    }

    @Test
    void getAllCategories_ShouldReturnList() {
        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryRes);

        List<CategoryRes> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(categoryRes.getName(), result.get(0).getName());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void getCategoryById_ShouldReturnCategory_WhenFound() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryRes);

        CategoryRes result = categoryService.getCategoryById(categoryId);

        assertNotNull(result);
        assertEquals(categoryRes.getName(), result.getName());
    }

    @Test
    void getCategoryById_ShouldThrowException_WhenNotFound() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> categoryService.getCategoryById(categoryId));
    }

    @Test
    void createCategory_ShouldReturnCategory_WhenValidRequest() {
        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryMapper.toEntity(categoryReq)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryRes);

        CategoryRes result = categoryService.createCategory(categoryReq);

        assertNotNull(result);
        assertEquals(categoryRes.getName(), result.getName());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void createCategory_ShouldThrowException_WhenNameExists() {
        when(categoryRepository.existsByName(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> categoryService.createCategory(categoryReq));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldReturnCategory_WhenFound() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryRes);

        CategoryRes result = categoryService.updateCategory(categoryId, categoryReq);

        assertNotNull(result);
        assertEquals(categoryRes.getName(), result.getName());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void updateCategory_ShouldThrowException_WhenNotFound() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> categoryService.updateCategory(categoryId, categoryReq));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldDelete_WhenFound() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository, times(1)).deleteById(categoryId);
    }

    @Test
    void deleteCategory_ShouldThrowException_WhenNotFound() {
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> categoryService.deleteCategory(categoryId));
        verify(categoryRepository, never()).deleteById(any(UUID.class));
    }
}
