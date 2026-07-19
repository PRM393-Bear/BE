package com.example.PRM.serviceImpl;

import com.example.PRM.service.OutfitService;
import com.example.PRM.service.UploadService;
import com.example.PRM.util.AuthDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutfitServiceImpl implements OutfitService {

    private final RestTemplate restTemplate;
    private final UploadService uploadService;
    private static final String AI_BASE_URL = "http://localhost:8000/api";
//    private static final String AI_BASE_URL = "https://brave-blessing-server.up.railway.app/api";
//    private static final String AI_BASE_URL = "http://api:8000/api";

    // ─────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────

    private UUID getUserId(Authentication authentication) {
        UsernamePasswordAuthenticationToken authToken =
                (UsernamePasswordAuthenticationToken) authentication;
        AuthDetails details = (AuthDetails) authToken.getDetails();
        return details.getUserId();
    }

    // ─────────────────────────────────────────
    // Phối đồ — 1 API duy nhất, dựa trên message
    // POST /api/wardrobe/{user_id}/outfits
    // Body: {"message": "..."}
    // ─────────────────────────────────────────

    @Override
    public ResponseEntity<?> getOutfits(
            Authentication authentication,
            String message,
            int maxOutfits
    ) {
        UUID userId = getUserId(authentication);
        String url  = AI_BASE_URL + "/wardrobe/" + userId + "/outfits?max_outfits=" + maxOutfits;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of("message", message == null ? "" : message);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "AI service lỗi: " + e.getMessage()));
        }
    }
}