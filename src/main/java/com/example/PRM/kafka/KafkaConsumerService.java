package com.example.PRM.kafka;

import com.example.PRM.dto.AuditLogEvent;
import com.example.PRM.entity.AuditLog;
import com.example.PRM.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaConsumerService {

    private final AuditLogRepository auditLogRepository;

    @KafkaListener(topics = "audit-log", groupId = "audit-log-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(AuditLogEvent event) {
        log.info("====== CONSUMER RECEIVED: {}", event);
        try {
            AuditLog logAudit = AuditLog.builder()
                    .userId(event.getUserId())
                    .username(event.getUsername())
                    .action(event.getAction())
                    .entity(event.getEntity())
                    .entityId(event.getEntityId())
                    .description(event.getDescription())
                    .ipAddress(event.getIpAddress())
                    .userAgent(event.getUserAgent())
                    .status(event.getStatus())
                    .build();

            auditLogRepository.save(logAudit);

            log.info("====== SAVED TO DB");

        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }
}