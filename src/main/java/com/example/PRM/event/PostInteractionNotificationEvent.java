package com.example.PRM.event;

import java.util.UUID;

public class PostInteractionNotificationEvent extends NotificationEvent {
    public PostInteractionNotificationEvent(Object source, UUID recipientId, String title, String message) {
        super(source, recipientId, title, message, "COMMUNITY_POST", true);
    }
}
