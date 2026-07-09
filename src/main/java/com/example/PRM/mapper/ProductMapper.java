package com.example.PRM.mapper;

import com.example.PRM.dto.request.ProductReq;
import com.example.PRM.dto.response.product.ProductRes;
import com.example.PRM.dto.response.product.ProductRes;
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
        if (product.getCategory() != null) {
            res.setCategory(product.getCategory().getName());
        }
        res.setType(product.getType());
        res.setCondition(product.getCondition());
        res.setPrice(product.getPrice());
        res.setSize(product.getSize());
        res.setColor(product.getColor());
        res.setImages(product.getImages());
        res.setAiTags(product.getAiTags());
        res.setStatus(product.getStatus());
        res.setRejectReason(product.getRejectReason());
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

    public ProductRes toProductRes(Product product) {
        ProductRes productLogRes = new ProductRes();
        productLogRes.setId(product.getId());
        productLogRes.setTitle(product.getTitle());
        productLogRes.setSellerName(product.getSeller().getUserName());
        productLogRes.setSellerId(product.getSeller().getUserId());
        return productLogRes;
    }
}