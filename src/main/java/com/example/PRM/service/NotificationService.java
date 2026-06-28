package com.example.PRM.service;

import com.example.PRM.entity.Notification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface NotificationService {
    void sendNotification(UUID userId, String title, String message, String type);
    List<Notification> getUserNotifications(UUID userId);
    void markAsRead(Long notificationId);
}
