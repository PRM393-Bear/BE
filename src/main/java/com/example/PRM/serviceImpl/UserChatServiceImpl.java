package com.example.PRM.serviceImpl;

import com.example.PRM.dto.response.ChatMessageRes;
import com.example.PRM.dto.response.ChatRoomRes;
import com.example.PRM.entity.ChatRoom;
import com.example.PRM.entity.User;
import com.example.PRM.repository.ChatMessageRepository;
import com.example.PRM.repository.ChatRoomRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.UserChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserChatServiceImpl implements UserChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Override
    public List<ChatMessageRes> getChatHistoryWithUser(UUID currentUserId, UUID otherUserId) {
        User u1 = userRepository.findByUserId(currentUserId).orElseThrow();
        User u2 = userRepository.findByUserId(otherUserId).orElseThrow();

        Optional<ChatRoom> roomOpt = chatRoomRepository.findRoomByUsers(u1, u2);
        if (roomOpt.isEmpty()) {
            return new ArrayList<>();
        }

        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomOpt.get().getId())
                .stream().map(msg -> {
                    ChatMessageRes res = new ChatMessageRes();
                    res.setId(msg.getId());
                    res.setRoomId(msg.getRoom().getId());
                    res.setSenderId(msg.getSender().getUserId());
                    res.setContent(msg.getContent());
                    res.setImageUrl(msg.getImageUrl());
                    res.setCreatedAt(msg.getCreatedAt());
                    res.setStatus(msg.getStatus().name());
                    return res;
                }).collect(Collectors.toList());
    }

    @Override
    public List<ChatRoomRes> getMyRooms(UUID currentUserId) {
        List<ChatRoom> rooms = chatRoomRepository.findAllByUserId(currentUserId);
        
        return rooms.stream().map(room -> {
            com.example.PRM.dto.response.ChatRoomRes res = new com.example.PRM.dto.response.ChatRoomRes();
            res.setRoomId(room.getId());
            res.setUpdatedAt(room.getUpdatedAt());

            User otherUser = room.getUser1().getUserId().equals(currentUserId) ? room.getUser2() : room.getUser1();
            res.setOtherUserId(otherUser.getUserId());
            res.setOtherUserName(otherUser.getUserName());
            res.setOtherUserLogo(otherUser.getLogoUrl());

            List<com.example.PRM.entity.ChatMessage> msgs = chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(room.getId());
            if (!msgs.isEmpty()) {
                com.example.PRM.entity.ChatMessage lastMsg = msgs.get(msgs.size() - 1);
                res.setLastMessage(lastMsg.getContent() != null && !lastMsg.getContent().isEmpty() 
                        ? lastMsg.getContent() 
                        : (lastMsg.getImageUrl() != null ? "[Hình ảnh]" : ""));
            }
            
            return res;
        }).collect(Collectors.toList());
    }
}
