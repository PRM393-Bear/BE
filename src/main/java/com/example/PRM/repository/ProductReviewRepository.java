package com.example.PRM.repository;

import com.example.PRM.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
    boolean existsByOrderId(UUID orderId);
}