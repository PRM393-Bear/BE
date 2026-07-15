package com.example.PRM.service;

import com.example.PRM.entity.Notification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface NotificationService {
    void sendNotification(UUID userId, String title, String message, String type);
    void sendNotification(UUID userId, String title, String message, String type, boolean saveToDb);
    List<Notification> getUserNotifications(UUID userId);
    List<Notification> getMyNotifications(UserDetails userDetails);
    void markAsRead(Long notificationId);
}
