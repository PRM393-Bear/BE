package com.example.PRM.controller;

import com.example.PRM.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/ingest")
    public ResponseEntity<?> ingestData(@RequestBody List<String> documents) {
        try {
            chatService.ingestDocumentData(documents);
            return ResponseEntity.ok(Map.of("message", "Nạp dữ liệu kiến thức thành công!"));
        } catch (Exception e) {
            log.error("Lỗi khi nạp dữ liệu", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi khi nạp dữ liệu: " + e.getMessage()));
        }
    }

    @GetMapping("/ask")
    public ResponseEntity<?> ask(@RequestParam String question) {
        try {
            String answer = chatService.askChatbot(question);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            log.error("Lỗi xử lý câu hỏi", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi xử lý câu hỏi: " + e.getMessage()));
        }
    }
}
