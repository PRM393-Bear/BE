package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.ProductReq;
import com.example.PRM.dto.response.ProductRes;
import com.example.PRM.entity.Product;
import com.example.PRM.entity.User;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.ProductMapper;
import com.example.PRM.repository.ProductRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserRepository userRepository;

    @Override
    public ProductRes getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
        return productMapper.toResponse(product);
    }

    @Override
    public List<ProductRes> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public ProductRes createProduct(ProductReq request) {

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("Type is required");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User seller = userRepository.findByUserName(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
        Product product = productMapper.toEntity(request, seller);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }
}