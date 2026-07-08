package com.example.PRM.repository;

import com.example.PRM.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    List<Product> findByCategoryNameAndPriceLessThanEqual(String categoryName, Long price);

    @Query("""
    SELECT p
    FROM Product p
    WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
    List<Product> findBySellerUserId(UUID sellerId);
}
