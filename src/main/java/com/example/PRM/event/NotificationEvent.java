package com.example.PRM.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public abstract class NotificationEvent extends ApplicationEvent {
    private final UUID recipientId;
    private final String title;
    private final String message;
    private final String type;
    private final boolean saveToDb;

    public NotificationEvent(Object source, UUID recipientId, String title, String message, String type, boolean saveToDb) {
        super(source);
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.saveToDb = saveToDb;
    }
}
