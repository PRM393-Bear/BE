package com.example.PRM.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class ChatReadReq {
    private UUID roomId;
    private UUID senderId; // ID của người đã gửi tin (người cần được đánh dấu là Đã xem)
}
