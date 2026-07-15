package com.example.PRM.event;

import java.util.UUID;

public class OrderNotificationEvent extends NotificationEvent {
    public OrderNotificationEvent(Object source, UUID recipientId, String title, String message) {
        super(source, recipientId, title, message, "ORDER", true);
    }
}
