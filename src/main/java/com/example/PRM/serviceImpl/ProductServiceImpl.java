package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.ProductFilterReq;
import com.example.PRM.dto.request.ProductReq;
import com.example.PRM.dto.response.ProductRes;
import com.example.PRM.entity.Product;
import com.example.PRM.entity.User;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.ProductMapper;
import com.example.PRM.repository.ProductRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.ProductService;
import com.example.PRM.util.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    @Override
    public List<ProductRes> search(String category, Long maxPrice) {
        return productRepository.findByCategoryAndPriceLessThanEqual(category, maxPrice)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public List<ProductRes> searchProductByKeyword(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }

        return productRepository.searchByKeyword(keyword.trim())
                .stream()
                .map(productMapper::toResponse)
                .toList();

    }

    @Override
    public ProductRes updateProduct(UUID id, ProductReq request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found product with id: " + id));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!product.getSeller().getUserName().equals(username)) {
            throw new ForbiddenException("You have no permission for this action");
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (request.getType() == null) {
            throw new IllegalArgumentException("Title is required");
        }

        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setType(request.getType());
        product.setCondition(request.getCondition());
        product.setPrice(request.getPrice());
        product.setSize(request.getSize());
        product.setColor(request.getColor());
        product.setImages(request.getImages());
        product.setAiTags(request.getAiTags());
        product.setStatus(request.getStatus());

        if (request.getLifecycleGeneration() != null) {
            product.setLifecycleGeneration(request.getLifecycleGeneration());
        }

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);

    }

    @Override
    public List<ProductRes> getProductsByUserId(UUID userId) {

        return productRepository.findBySellerUserId(userId)
                .stream()
                .map(productMapper::toResponse)
                .toList();

    }

    @Override
    public ProductRes hideProduct(UUID id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found product with id: " + id));

        product.setStatus(Product.ProductStatus.HIDDEN);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);

    }

    @Override
    public List<ProductRes> filterProducts(ProductFilterReq filter) {
        Specification<Product> spec = ProductSpecification.filterProducts(filter);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (filter.getSortBy() != null && !filter.getSortBy().isBlank()) {
            switch (filter.getSortBy()) {
                case "priceAsc":
                    sort = Sort.by(Sort.Direction.ASC, "price");
                    break;
                case "priceDesc":
                    sort = Sort.by(Sort.Direction.DESC, "price");
                    break;
                case "newest":
                default:
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }
        }

        return productRepository.findAll(spec, sort)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }
}