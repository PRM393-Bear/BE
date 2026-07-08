package com.example.PRM.repository;

import com.example.PRM.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUserName(String username);
    boolean existsByUserName(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUserId(UUID userId);
    Optional<User> findByEmail(String email);

    List<User> findByRole_RoleName(String admin);
    List<User> findByVerified(boolean verified);
    long countByVerified(boolean verified);
}
