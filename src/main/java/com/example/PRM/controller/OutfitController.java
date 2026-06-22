package com.example.PRM.controller;

import com.example.PRM.dto.request.ChatRequestDto;
import com.example.PRM.entity.User;
import com.example.PRM.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/outfit")
public class OutfitController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final UserRepository userRepository;
    private static final String AI_SERVICE_URL = "http://localhost:8000/api/chat";

    public OutfitController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/suggest")
    public ResponseEntity<?> suggestOutfit(
            @RequestBody ChatRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        // userDetails.getUsername() lấy từ JWT đã xác thực
        String userName = userDetails.getUsername();

        // Tra UUID thật từ username
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> body = new HashMap<>();
        body.put("message", dto.getMessage());
        body.put("user_id", user.getUserId().toString());  // UUID thật
        body.put("wardrobe_item_id", dto.getWardrobeItemId());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                AI_SERVICE_URL, body, Map.class
        );

        return ResponseEntity.ok(response.getBody());
    }
}
