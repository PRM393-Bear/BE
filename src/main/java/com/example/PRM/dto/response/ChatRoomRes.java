package com.example.PRM.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ChatRoomRes {
    private UUID roomId;
    private UUID otherUserId;
    private String otherUserName;
    private String otherUserLogo;
    private String lastMessage;
    private OffsetDateTime updatedAt;
}
