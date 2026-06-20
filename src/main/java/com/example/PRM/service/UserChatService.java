package com.example.PRM.service;

import com.example.PRM.dto.response.ChatMessageRes;
import java.util.List;
import java.util.UUID;

import com.example.PRM.dto.response.ChatRoomRes;

public interface UserChatService {
    List<ChatMessageRes> getChatHistoryWithUser(UUID currentUserId, UUID otherUserId);
    List<ChatRoomRes> getMyRooms(UUID currentUserId);
}
