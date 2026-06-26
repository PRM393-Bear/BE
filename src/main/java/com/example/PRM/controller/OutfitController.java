package com.example.PRM.controller;

import com.example.PRM.dto.response.UploadRes;
import com.example.PRM.service.UploadService;
import com.example.PRM.util.AuthDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/outfit")
@RequiredArgsConstructor
public class OutfitController {

    private final RestTemplate restTemplate;
    private final UploadService uploadService;
    private static final String AI_BASE_URL = "http://brave-blessing-server.up.railway.app/api";

    private UUID getUserId(Authentication authentication) {
        UsernamePasswordAuthenticationToken authToken =
                (UsernamePasswordAuthenticationToken) authentication;
        AuthDetails details = (AuthDetails) authToken.getDetails();
        return details.getUserId();
    }

    private String getUserName(Authentication authentication) {
        return authentication.getName();
    }

    /**
     * Nhận base64 từ Python → decode → upload Cloudinary → trả URL
     */
    private String uploadBase64ToCloudinary(String base64Image, String username) {
        // Bỏ prefix "data:image/png;base64,"
        String base64Data = base64Image.contains(",")
                ? base64Image.split(",")[1]
                : base64Image;

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        // Chuyển thành MockMultipartFile để dùng UploadService có sẵn
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "outfit.png",
                "image/png",
                imageBytes
        );

        UploadRes uploaded = uploadService.uploadImage(file, username);
        return uploaded.getUrl(); // trả về URL Cloudinary
    }

    // ─────────────────────────────────────────
    // 1. Phối đồ tự động có ảnh
    // GET /api/outfit/image?max_outfits=3
    // ─────────────────────────────────────────

    @GetMapping("/image")
    public ResponseEntity<?> getOutfitImage(
            Authentication authentication,
            @RequestParam(defaultValue = "3") int maxOutfits
    ) {
        UUID userId   = getUserId(authentication);
        String username = getUserName(authentication);

        String url = AI_BASE_URL + "/wardrobe/" + userId + "/outfits/image?max_outfits=" + maxOutfits;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            // Upload ảnh lên Cloudinary
            if (body != null && body.containsKey("outfit_image")) {
                String base64 = (String) body.get("outfit_image");
                String cloudinaryUrl = uploadBase64ToCloudinary(base64, username);
                body.put("outfit_image", cloudinaryUrl);
            }

            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "AI service lỗi: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────
    // 2. Phối đồ theo dịp có ảnh
    // POST /api/outfit/occasion/image
    // Body: {"message": "lựa cho tôi bộ đi đám cưới"}
    // ─────────────────────────────────────────

    @PostMapping("/occasion/image")
    public ResponseEntity<?> getOutfitOccasionImage(
            Authentication authentication,
            @RequestBody Map<String, String> body,
            @RequestParam(defaultValue = "3") int maxOutfits
    ) {
        UUID userId     = getUserId(authentication);
        String username = getUserName(authentication);

        String url = AI_BASE_URL + "/wardrobe/" + userId + "/outfits/occasion/image?max_outfits=" + maxOutfits;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            // Upload ảnh lên Cloudinary
            if (responseBody != null && responseBody.containsKey("outfit_image")) {
                String base64 = (String) responseBody.get("outfit_image");
                String cloudinaryUrl = uploadBase64ToCloudinary(base64, username);
                responseBody.put("outfit_image", cloudinaryUrl);
            }

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "AI service lỗi: " + e.getMessage()));
        }
    }
}