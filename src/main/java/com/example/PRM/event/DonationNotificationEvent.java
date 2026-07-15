package com.example.PRM.event;

import java.util.UUID;

public class DonationNotificationEvent extends NotificationEvent {
    public DonationNotificationEvent(Object source, UUID recipientId, String title, String message) {
        super(source, recipientId, title, message, "DONATION", true);
    }
}
