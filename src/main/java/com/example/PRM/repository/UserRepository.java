package com.example.PRM.repository;

import com.example.PRM.dto.request.UserReq;
import com.example.PRM.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUserName(String username);
    boolean existsByUserName(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUserId(UUID userId);
    Optional<User> findByEmail(String email);

    List<User> findByRole_RoleName(String roleName);

    // Derived method: dùng "Verified" (không có "Is"), vì Spring parse theo getter isVerified() -> property "verified"
    List<User> findByIsVerified(boolean verified);

    // JPQL thủ công: dùng "isVerified" (đúng tên field, vì Hibernate field access)
    @Query("SELECT COUNT(u) FROM User u WHERE u.isVerified = :verified")
    long countByVerified(@Param("verified") boolean verified);

    List<User> findByIsBlocked(boolean blocked);

}
