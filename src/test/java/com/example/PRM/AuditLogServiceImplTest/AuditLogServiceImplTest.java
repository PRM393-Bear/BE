package com.example.PRM.serviceImpl;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(auditLogService, "auditServiceBaseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(auditLogService, "auditServiceApiKey", "test-api-key");
        ReflectionTestUtils.setField(auditLogService, "restTemplate", restTemplate);
    }

    @Test
    void log_ShouldSendAuditLog_WhenValidRequest() {
        // Arrange
        String action = "CREATE";
        String entity = "USER";
        String entityId = "123";
        String description = "Created user";
        String status = "SUCCESS";
        UUID userId = UUID.randomUUID();
        String username = "testuser";

        when(request.getHeader("User-Agent")).thenReturn("Mozilla");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        // Act
        auditLogService.log(action, entity, entityId, description, status, userId, username, request);

        // Assert
        verify(restTemplate, times(1)).postForEntity(
                eq("http://localhost:8080/api/audit-logs"),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void log_ShouldHandleException_WhenRestTemplateThrowsException() {
        // Arrange
        String action = "CREATE";
        String entity = "USER";
        String entityId = "123";
        String description = "Created user";
        String status = "SUCCESS";
        UUID userId = UUID.randomUUID();
        String username = "testuser";

        when(request.getHeader("User-Agent")).thenReturn("Mozilla");
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        // Act
        auditLogService.log(action, entity, entityId, description, status, userId, username, request);

        // Assert
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Void.class));
        // Verify that the exception is caught and not propagated (handled by try-catch)
    }
    
    @Test
    void log_ShouldHandleNullRequestAndDescription() {
        // Arrange
        String action = "CREATE";
        String entity = "USER";
        String entityId = "123";
        String status = "SUCCESS";
        UUID userId = UUID.randomUUID();
        String username = "testuser";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        // Act
        auditLogService.log(action, entity, entityId, null, status, userId, username, null);

        // Assert
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Void.class));
    }
}
