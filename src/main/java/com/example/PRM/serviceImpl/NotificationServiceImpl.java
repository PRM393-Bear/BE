package com.example.PRM.serviceImpl;

import com.example.PRM.entity.Notification;
import com.example.PRM.entity.User;
import com.example.PRM.repository.NotificationRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;


    @Override
    public void sendNotification(UUID userId, String title, String message, String type) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User is not available"));
        Notification notification = Notification.builder()
                .recipient(user)
                .title(title)
                .message(message)
                .type(type)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findByRecipient_UserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification is not available"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
