package com.example.PRM.serviceImpl;

import com.example.PRM.dto.AuditLogEvent;
import com.example.PRM.kafka.KafkaProducerService;
import com.example.PRM.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final KafkaProducerService kafkaProducerService;

    @Override
    public void log(String action, String entity, String entityId,
                    String description, String status,
                    UUID userId, String username,
                    HttpServletRequest request) {
        System.out.println(">>> AuditLogService Called");

        AuditLogEvent event = AuditLogEvent.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .description(description)
                .ipAddress(getClientIp(request))
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .status(status)
                .build();

        kafkaProducerService.sendAuditLog(event);
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