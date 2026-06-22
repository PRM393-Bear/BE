package com.example.PRM.mapper;

import com.example.PRM.dto.request.ProductReq;
import com.example.PRM.dto.response.ProductRes;
import com.example.PRM.entity.Product;
import com.example.PRM.entity.User;
import com.example.PRM.status_enum.ProductStatus;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductRes toResponse(Product product) {
        ProductRes res = new ProductRes();
        res.setId(product.getId());
        res.setTitle(product.getTitle());
        res.setDescription(product.getDescription());
        res.setCategory(product.getCategory());
        res.setType(product.getType());
        res.setCondition(product.getCondition());
        res.setPrice(product.getPrice());
        res.setSize(product.getSize());
        res.setColor(product.getColor());
        res.setImages(product.getImages());
        res.setAiTags(product.getAiTags());
        res.setStatus(product.getStatus());
        res.setLifecycleGeneration(product.getLifecycleGeneration());
        res.setCreatedAt(product.getCreatedAt());

        if (product.getSeller() != null) {
            res.setSellerId(product.getSeller().getUserId());
            res.setSellerName(product.getSeller().getFullName());
        }
        return res;
    }

    public Product toEntity(ProductReq req, User seller) {
        Product product = new Product();
        product.setSeller(seller);
        product.setTitle(req.getTitle());
        product.setDescription(req.getDescription());
        product.setCategory(req.getCategory());
        product.setType(req.getType());
        product.setCondition(req.getCondition());
        product.setPrice(req.getPrice());
        product.setSize(req.getSize());
        product.setColor(req.getColor());
        product.setImages(req.getImages());
        product.setAiTags(req.getAiTags());
        product.setStatus(ProductStatus.AVAILABLE);
        product.setLifecycleGeneration(
                req.getLifecycleGeneration() != null ? req.getLifecycleGeneration() : (short) 1
        );
        return product;
    }
}