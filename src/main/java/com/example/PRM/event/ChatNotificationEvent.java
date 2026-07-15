package com.example.PRM.event;

import java.util.UUID;

public class ChatNotificationEvent extends NotificationEvent {
    public ChatNotificationEvent(Object source, UUID recipientId, String title, String message) {
        // Chat messages are pushed via WebSocket but not saved to the notification table to save space.
        super(source, recipientId, title, message, "CHAT", false);
    }
}
