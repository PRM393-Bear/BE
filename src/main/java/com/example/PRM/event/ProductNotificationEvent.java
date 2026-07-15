package com.example.PRM.event;

import java.util.UUID;

public class ProductNotificationEvent extends NotificationEvent {
    public ProductNotificationEvent(Object source, UUID recipientId, String title, String message) {
        super(source, recipientId, title, message, "PRODUCT", true);
    }
}
