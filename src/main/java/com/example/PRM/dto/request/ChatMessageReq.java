package com.example.PRM.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class ChatMessageReq {
    private UUID receiverId;
    private String content;
    private String imageUrl;
}
