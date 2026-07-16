package com.example.PRM.serviceImpl;

import com.example.PRM.entity.Notification;
import com.example.PRM.entity.User;
import com.example.PRM.repository.NotificationRepository;
import com.example.PRM.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserDetails userDetails;

    private User user;
    private Notification notification;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new User();
        user.setUserId(userId);
        user.setUserName("testuser");

        notification = Notification.builder()
                .id(1L)
                .recipient(user)
                .title("Test Title")
                .message("Test Message")
                .type("INFO")
                .build();
    }

    @Test
    void sendNotification_ShouldSaveAndSend_WhenSaveToDbIsTrue() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.sendNotification(userId, "Test Title", "Test Message", "INFO", true);

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("testuser"), eq("/queue/notifications"), any(Notification.class));
    }

    @Test
    void sendNotification_ShouldNotSaveAndSend_WhenSaveToDbIsFalse() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        notificationService.sendNotification(userId, "Test Title", "Test Message", "INFO", false);

        verify(notificationRepository, never()).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("testuser"), eq("/queue/notifications"), any(Notification.class));
    }

    @Test
    void sendNotification_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            notificationService.sendNotification(userId, "Test Title", "Test Message", "INFO")
        );
        
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any(Notification.class));
    }

    @Test
    void getUserNotifications_ShouldReturnList() {
        when(notificationRepository.findByRecipient_UserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Collections.singletonList(notification));

        List<Notification> result = notificationService.getUserNotifications(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).findByRecipient_UserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void getMyNotifications_ShouldReturnList_WhenUserExists() {
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));
        when(notificationRepository.findByRecipient_UserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Collections.singletonList(notification));

        List<Notification> result = notificationService.getMyNotifications(userDetails);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getMyNotifications_ShouldThrowException_WhenUserNotFound() {
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> notificationService.getMyNotifications(userDetails));
    }

    @Test
    void markAsRead_ShouldUpdateReadStatus_WhenNotificationExists() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        
        notificationService.markAsRead(1L);

        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void markAsRead_ShouldThrowException_WhenNotificationNotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> notificationService.markAsRead(1L));
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}
