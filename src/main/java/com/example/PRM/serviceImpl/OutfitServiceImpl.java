package com.example.PRM.serviceImpl;

import com.example.PRM.service.OutfitService;
import com.example.PRM.service.UploadService;
import com.example.PRM.util.AuthDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutfitServiceImpl implements OutfitService {

    private final RestTemplate restTemplate;
    private final UploadService uploadService;
    private static final String AI_BASE_URL = "http://localhost:8000/api";

    // ─────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────

    private UUID getUserId(Authentication authentication) {
        UsernamePasswordAuthenticationToken authToken =
                (UsernamePasswordAuthenticationToken) authentication;
        AuthDetails details = (AuthDetails) authToken.getDetails();
        return details.getUserId();
    }

    private String getUsername(Authentication authentication) {
        return authentication.getName();
    }

    private String uploadBase64ToCloudinary(String base64Image, String username) {
        String base64Data = base64Image.contains(",")
                ? base64Image.split(",")[1]
                : base64Image;

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "outfit.png",
                "image/png",
                imageBytes
        );

        return uploadService.uploadImage(file, username).getUrl();
    }

    /**
     * Xử lý list outfits — upload từng ảnh base64 lên Cloudinary
     * thay thế image_b64 bằng image_url
     */
    private void processOutfitImages(List<Map<String, Object>> outfits, String username) {
        for (Map<String, Object> outfit : outfits) {
            if (outfit.containsKey("image_b64")) {
                try {
                    String base64        = (String) outfit.get("image_b64");
                    String cloudinaryUrl = uploadBase64ToCloudinary(base64, username);
                    outfit.put("image_url", cloudinaryUrl);
                } catch (Exception e) {
                    outfit.put("image_url", null);
                    System.out.println("Upload Cloudinary lỗi: " + e.getMessage());
                } finally {
                    outfit.remove("image_b64");  // xóa base64 khỏi response
                }
            }
        }
    }

    // ─────────────────────────────────────────
    // 1. Phối đồ tự động có ảnh
    // ─────────────────────────────────────────

    @Override
    public ResponseEntity<?> getOutfitImage(Authentication authentication, int maxOutfits) {
        UUID userId     = getUserId(authentication);
        String username = getUsername(authentication);
        String url      = AI_BASE_URL + "/wardrobe/" + userId + "/outfits/image?max_outfits=" + maxOutfits;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("outfits")) {
                List<Map<String, Object>> outfits =
                        (List<Map<String, Object>>) body.get("outfits");
                processOutfitImages(outfits, username);
            }

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "AI service lỗi: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────
    // 2. Phối đồ theo dịp có ảnh
    // ─────────────────────────────────────────

    @Override
    public ResponseEntity<?> getOutfitOccasionImage(
            Authentication authentication,
            Map<String, String> body,
            int maxOutfits
    ) {
        UUID userId     = getUserId(authentication);
        String username = getUsername(authentication);
        String url      = AI_BASE_URL + "/wardrobe/" + userId
                + "/outfits/occasion/image?max_outfits=" + maxOutfits;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("outfits")) {
                List<Map<String, Object>> outfits =
                        (List<Map<String, Object>>) responseBody.get("outfits");
                processOutfitImages(outfits, username);
            }

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "AI service lỗi: " + e.getMessage()));
        }
    }
}