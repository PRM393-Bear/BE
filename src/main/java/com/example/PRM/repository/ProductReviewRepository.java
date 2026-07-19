package com.example.PRM.repository;

import com.example.PRM.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
    boolean existsByOrderId(UUID orderId);

    @Query("SELECT r FROM ProductReview r WHERE r.order.seller.userName = :sellerUsername")
    List<ProductReview> findBySellerUsername(@Param("sellerUsername") String sellerUsername);
}