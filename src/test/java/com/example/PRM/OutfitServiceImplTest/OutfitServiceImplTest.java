package com.example.PRM.serviceImpl;

import com.example.PRM.service.UploadService;
import com.example.PRM.util.AuthDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    void getOutfits_ShouldReturnResponse_WhenServiceIsSuccessful() {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        
        ResponseEntity<Map> mockResponse = ResponseEntity.ok(responseBody);
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(mockResponse);

        ResponseEntity<?> response = outfitService.getOutfits(authentication, 5);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(responseBody, response.getBody());
    }

    @Test
    void getOutfits_ShouldReturnErrorResponse_WhenServiceThrowsException() {
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        ResponseEntity<?> response = outfitService.getOutfits(authentication, 5);

        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
        
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("AI service lỗi: Connection timeout", body.get("error"));
    }

    @Test
    void getOutfitsByOccasion_ShouldReturnResponse_WhenServiceIsSuccessful() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("message", "gợi ý đồ đi biển");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        
        ResponseEntity<Map> mockResponse = ResponseEntity.ok(responseBody);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class))).thenReturn(mockResponse);

        ResponseEntity<?> response = outfitService.getOutfitsByOccasion(authentication, requestBody, 5);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(responseBody, response.getBody());
    }

    @Test
    void getOutfitsByOccasion_ShouldReturnErrorResponse_WhenServiceThrowsException() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("message", "gợi ý đồ đi biển");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        ResponseEntity<?> response = outfitService.getOutfitsByOccasion(authentication, requestBody, 5);

        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
        
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("AI service lỗi: Connection timeout", body.get("error"));
    }
}
