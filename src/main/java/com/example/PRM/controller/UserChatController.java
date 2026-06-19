package com.example.PRM.controller;

import com.example.PRM.dto.response.ChatMessageRes;
import com.example.PRM.service.UserChatService;
import com.example.PRM.util.AuthDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class UserChatController {

    private final UserChatService userChatService;

    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<List<ChatMessageRes>> getHistory(
            @PathVariable UUID otherUserId,
            Authentication authentication
    ) {
        AuthDetails authDetails = (AuthDetails) authentication.getDetails();
        return ResponseEntity.ok(userChatService.getChatHistoryWithUser(authDetails.getUserId(), otherUserId));
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<com.example.PRM.dto.response.ChatRoomRes>> getMyRooms(Authentication authentication) {
        AuthDetails authDetails = (AuthDetails) authentication.getDetails();
        return ResponseEntity.ok(userChatService.getMyRooms(authDetails.getUserId()));
    }
}
