package com.example.PRM.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class ChatReadReq {
    private UUID roomId;
    private UUID senderId;
}
