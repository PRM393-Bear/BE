package com.example.PRM.repository;

import com.example.PRM.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByCategoryAndPriceLessThanEqual(String category, Long price);

}
