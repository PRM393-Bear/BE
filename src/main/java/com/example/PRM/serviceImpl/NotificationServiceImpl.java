package com.example.PRM.serviceImpl;

import com.example.PRM.entity.Notification;
import com.example.PRM.entity.User;
import com.example.PRM.repository.NotificationRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNotification(UUID userId, String title, String message, String type) {
        sendNotification(userId, title, message, type, true);
    }

    @Override
    public void sendNotification(UUID userId, String title, String message, String type, boolean saveToDb) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User is not available"));
        Notification notification = Notification.builder()
                .recipient(user)
                .title(title)
                .message(message)
                .type(type)
                .build();
        
        if (saveToDb) {
            notification = notificationRepository.save(notification);
        } else {
            notification.setId(System.currentTimeMillis()); // Temporary ID for frontend keying
            notification.setCreatedAt(java.time.LocalDateTime.now());
        }

        // Bắn thông báo qua WebSocket
        messagingTemplate.convertAndSendToUser(
                user.getUserName(), 
                "/queue/notifications", 
                notification
        );
    }

    @Override
    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findByRecipient_UserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> getMyNotifications(UserDetails userDetails) {
        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByRecipient_UserIdOrderByCreatedAtDesc(user.getUserId());
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification is not available"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
