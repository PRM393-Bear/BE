package com.example.PRM.kafka;

import com.example.PRM.dto.AuditLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, AuditLogEvent> kafkaTemplate;
    private static final String TOPIC = "audit-log";

    public void sendAuditLog(AuditLogEvent event) {
        kafkaTemplate.send(TOPIC, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka send failed: {}", ex.getMessage());
                    } else {
                        log.info("Audit log sent: {} - {}", event.getAction(), event.getEntity());
                    }
                });
    }
}