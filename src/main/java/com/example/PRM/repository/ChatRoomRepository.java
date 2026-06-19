package com.example.PRM.repository;

import com.example.PRM.entity.ChatRoom;
import com.example.PRM.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    @Query("SELECT c FROM ChatRoom c WHERE (c.user1 = :u1 AND c.user2 = :u2) OR (c.user1 = :u2 AND c.user2 = :u1)")
    Optional<ChatRoom> findRoomByUsers(@Param("u1") User u1, @Param("u2") User u2);

    @Query("SELECT c FROM ChatRoom c WHERE c.user1.userId = :userId OR c.user2.userId = :userId ORDER BY c.updatedAt DESC")
    List<ChatRoom> findAllByUserId(@Param("userId") UUID userId);
}
