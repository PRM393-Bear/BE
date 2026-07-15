package com.example.PRM.repository;

import com.example.PRM.entity.WardrobeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WardrobeItemRepository extends JpaRepository<WardrobeItem, UUID> {
    Optional<WardrobeItem> findById(UUID wardrobeItemId);
    Optional<WardrobeItem> findByName(String name);
}
