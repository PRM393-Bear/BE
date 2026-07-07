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
//    private static final String AI_BASE_URL = "http://localhost:8000/api";
//    private static final String AI_BASE_URL = "https://brave-blessing-server.up.railway.app/api";
    private static final String AI_BASE_URL = "http://api:8000/api";

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

    @Override
    public ResponseEntity<?> getOutfits(Authentication authentication, int maxOutfits) {
        UUID userId = getUserId(authentication);
        String url  = AI_BASE_URL + "/wardrobe/" + userId + "/outfits?max_outfits=" + maxOutfits;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "AI service lỗi: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────
    // 2. Phối đồ theo dịp
    // POST /api/outfit/occasion
    // Body: {"message": "gợi ý đồ đi biển"}
    // ─────────────────────────────────────────

    @Override
    public ResponseEntity<?> getOutfitsByOccasion(
            Authentication authentication,
            Map<String, String> body,
            int maxOutfits
    ) {
        UUID userId = getUserId(authentication);
        String url  = AI_BASE_URL + "/wardrobe/" + userId
                + "/outfits/occasion?max_outfits=" + maxOutfits;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "AI service lỗi: " + e.getMessage()));
        }
    }
}