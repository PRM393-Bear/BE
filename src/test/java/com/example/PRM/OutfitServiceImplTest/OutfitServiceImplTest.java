package com.example.PRM.OutfitServiceImplTest;

import com.example.PRM.service.UploadService;
import com.example.PRM.serviceImpl.OutfitServiceImpl;
import com.example.PRM.util.AuthDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutfitServiceImplTest {

    @InjectMocks
    private OutfitServiceImpl outfitService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UploadService uploadService;

    private UsernamePasswordAuthenticationToken authentication;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        AuthDetails authDetails = new AuthDetails(userId);

        authentication = new UsernamePasswordAuthenticationToken("testuser", "password");
        authentication.setDetails(authDetails);
    }

    // ─────────────────────────────────────────
    // getOutfits — success, message present
    // ─────────────────────────────────────────

    @Test
    void getOutfits_ShouldReturnResponse_WhenServiceIsSuccessful() {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);

        ResponseEntity<Map> mockResponse = ResponseEntity.ok(responseBody);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponse);

        ResponseEntity<?> response = outfitService.getOutfits(authentication, "gợi ý đồ đi biển", 5);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(responseBody, response.getBody());
    }

    // ─────────────────────────────────────────
    // getOutfits — verify URL & body construction
    // ─────────────────────────────────────────

    @Test
    void getOutfits_ShouldBuildCorrectUrlAndBody_WhenMessageProvided() {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        ResponseEntity<Map> mockResponse = ResponseEntity.ok(responseBody);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.postForEntity(urlCaptor.capture(), entityCaptor.capture(), eq(Map.class)))
                .thenReturn(mockResponse);

        outfitService.getOutfits(authentication, "gợi ý đồ đi biển", 5);

        String capturedUrl = urlCaptor.getValue();
        assertEquals(true, capturedUrl.contains("/wardrobe/" + userId + "/outfits"));
        assertEquals(true, capturedUrl.contains("max_outfits=5"));

        @SuppressWarnings("unchecked")
        Map<String, Object> capturedBody = (Map<String, Object>) entityCaptor.getValue().getBody();
        assertNotNull(capturedBody);
        assertEquals("gợi ý đồ đi biển", capturedBody.get("message"));
    }

    // ─────────────────────────────────────────
    // getOutfits — message null -> fallback thành chuỗi rỗng
    // ─────────────────────────────────────────

    @Test
    void getOutfits_ShouldFallbackMessageToEmptyString_WhenMessageIsNull() {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        ResponseEntity<Map> mockResponse = ResponseEntity.ok(responseBody);

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.postForEntity(anyString(), entityCaptor.capture(), eq(Map.class)))
                .thenReturn(mockResponse);

        ResponseEntity<?> response = outfitService.getOutfits(authentication, null, 3);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        @SuppressWarnings("unchecked")
        Map<String, Object> capturedBody = (Map<String, Object>) entityCaptor.getValue().getBody();
        assertEquals("", capturedBody.get("message"));
    }

    // ─────────────────────────────────────────
    // getOutfits — exception
    // ─────────────────────────────────────────

    @Test
    void getOutfits_ShouldReturnErrorResponse_WhenServiceThrowsException() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        ResponseEntity<?> response = outfitService.getOutfits(authentication, "gợi ý đồ đi biển", 5);

        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("AI service lỗi: Connection timeout", body.get("error"));
    }

    @Test
    void getOutfits_ShouldReturnErrorResponse_WhenServiceThrowsException_AndMessageIsNull() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        ResponseEntity<?> response = outfitService.getOutfits(authentication, null, 5);

        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("AI service lỗi: Connection timeout", body.get("error"));
    }
}