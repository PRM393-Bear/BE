package com.example.PRM.repository;

import com.example.PRM.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(UUID roomId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.status = :newStatus WHERE m.room.id = :roomId AND m.sender.userId = :senderId AND m.status = :oldStatus")
    int markMessagesAsRead(@Param("roomId") UUID roomId, @Param("senderId") UUID senderId, @Param("oldStatus") com.example.PRM.entity.MessageStatus oldStatus, @Param("newStatus") com.example.PRM.entity.MessageStatus newStatus);
}
