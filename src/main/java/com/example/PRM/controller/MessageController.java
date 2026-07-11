package com.example.PRM.controller;

import com.example.PRM.dto.request.chat.ChatMessageReq;
import com.example.PRM.dto.request.chat.ChatReadReq;
import com.example.PRM.dto.response.ChatMessageRes;
import com.example.PRM.entity.ChatMessage;
import com.example.PRM.entity.ChatRoom;
import com.example.PRM.entity.User;
import com.example.PRM.repository.ChatMessageRepository;
import com.example.PRM.repository.ChatRoomRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.status_enum.MessageStatus;
import com.example.PRM.util.AuthDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    // FE sẽ push tin nhắn vào /app/chat.send
    @MessageMapping("/chat.send")
    @Transactional
    public void sendMessage(@Payload ChatMessageReq req, java.security.Principal principal) {
        System.out.println("====== [WebSocket] NHẬN ĐƯỢC TIN NHẮN TỪ CLIENT ======");
        System.out.println("Nội dung: " + req.getContent());
        
        if (principal == null) {
            System.out.println("LỖI: Principal (Người gửi) bị NULL. Có thể chưa xác thực JWT thành công!");
            return;
        }

        Authentication authentication = (Authentication) principal;
        AuthDetails authDetails = (AuthDetails) authentication.getDetails();
        User sender = userRepository.findByUserId(authDetails.getUserId()).orElseThrow(() -> new RuntimeException("Không tìm thấy Sender"));
        User receiver = userRepository.findByUserId(req.getReceiverId()).orElseThrow(() -> new RuntimeException("Không tìm thấy Receiver UUID: " + req.getReceiverId()));


        // 1. Lấy hoặc tự động tạo phòng chat giữa 2 người
        ChatRoom room = chatRoomRepository.findRoomByUsers(sender, receiver)
                .orElseGet(() -> {
                    ChatRoom newRoom = new ChatRoom();
                    newRoom.setUser1(sender);
                    newRoom.setUser2(receiver);
                    return chatRoomRepository.save(newRoom);
                });

        // 2. Lưu tin nhắn vào Database
        ChatMessage msg = new ChatMessage();
        msg.setRoom(room);
        msg.setSender(sender);
        msg.setContent(req.getContent());
        msg.setImageUrl(req.getImageUrl());
        ChatMessage savedMsg = chatMessageRepository.save(msg);

        // 3. Map sang Response
        ChatMessageRes res = new ChatMessageRes();
        res.setId(savedMsg.getId());
        res.setRoomId(room.getId());
        res.setSenderId(sender.getUserId());
        res.setContent(savedMsg.getContent());
        res.setImageUrl(savedMsg.getImageUrl());
        res.setCreatedAt(savedMsg.getCreatedAt());
        res.setStatus(savedMsg.getStatus().name());

        // 4. Bắn luồng WebSocket thời gian thực tới CẢ 2 người bằng UserName (vì Spring STOMP nhận diện User qua Principal Name)
        messagingTemplate.convertAndSendToUser(sender.getUserName(), "/queue/messages", res);
        messagingTemplate.convertAndSendToUser(receiver.getUserName(), "/queue/messages", res);
    }

    // FE sẽ push tín hiệu đã đọc vào /app/chat.read
    @MessageMapping("/chat.read")
    @Transactional
    public void markAsRead(@Payload ChatReadReq req, Authentication authentication) {
        if (authentication == null) return;
        
        AuthDetails authDetails = (AuthDetails) authentication.getDetails();
        User reader = userRepository.findByUserId(authDetails.getUserId()).orElseThrow();
        User sender = userRepository.findByUserId(req.getSenderId()).orElseThrow();

        // Tìm tất cả các tin nhắn do sender gửi trong phòng này mà trạng thái đang là SENT
        int updatedCount = chatMessageRepository.markMessagesAsRead(req.getRoomId(), sender.getUserId(), MessageStatus.SENT, MessageStatus.READ);

        if (updatedCount > 0) {
            // Gửi thông báo lại cho người gửi (sender) biết là reader đã đọc tin nhắn của phòng này
            // Tạo 1 object nhỏ nhắn để báo hiệu
            java.util.Map<String, Object> receipt = new java.util.HashMap<>();
            receipt.put("type", "READ_RECEIPT");
            receipt.put("roomId", req.getRoomId());
            receipt.put("readerId", reader.getUserId());
            
            messagingTemplate.convertAndSendToUser(sender.getUserName(), "/queue/receipts", receipt);
        }
    }
}
