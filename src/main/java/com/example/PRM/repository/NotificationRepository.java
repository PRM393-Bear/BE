package com.example.PRM.repository;

import com.example.PRM.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipient_UserIdOrderByCreatedAtDesc(UUID userId);
    long countByRecipient_UserIdAndIsReadFalse(UUID userId);
}
