package com.example.PRM.serviceImpl;

import com.example.PRM.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    @Value("${audit.service.base-url}")
    private String auditServiceBaseUrl;

    @Value("${audit.service.api-key}")
    private String auditServiceApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Async
    public void log(String action, String entity, String entityId,
                    String description, String status,
                    UUID userId, String username,
                    HttpServletRequest request) {

        String ipAddress = getClientIp(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        // Gộp các field audit-service chưa có sẵn cột riêng vào detail
        String detail = String.format(
                "%s | status=%s | ip=%s | userAgent=%s",
                description != null ? description : "",
                status,
                ipAddress,
                userAgent
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-KEY", auditServiceApiKey);

            Map<String, String> body = new HashMap<>();
            body.put("action", action);
            body.put("entity", entity);
            body.put("entityId", entityId);
            body.put("detail", detail);
            body.put("username", username);
            body.put("userId", userId != null ? userId.toString() : null);
            body.put("sourceService", "PRM");

            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(auditServiceBaseUrl + "/api/audit-logs", httpEntity, Void.class);

        } catch (Exception e) {
            log.error("Failed to send audit log to audit-service: {}", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}