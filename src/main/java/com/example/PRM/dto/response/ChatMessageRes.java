package com.example.PRM.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ChatMessageRes {
    private UUID id;
    private UUID roomId;
    private UUID senderId;
    private String content;
    private String imageUrl;
    private OffsetDateTime createdAt;
    private String status; // SENT, READ
}
