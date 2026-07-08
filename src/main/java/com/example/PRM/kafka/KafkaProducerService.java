package com.example.PRM.kafka;

import com.example.PRM.dto.AuditLogEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, AuditLogEvent> kafkaTemplate;
    private static final String TOPIC = "audit-log";

    public KafkaProducerService(@Autowired(required = false) KafkaTemplate<String, AuditLogEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAuditLog(AuditLogEvent event) {
        if (kafkaTemplate == null) {
            log.info("Kafka is disabled, skipping audit log send: {} - {}", event.getAction(), event.getEntity());
            return;
        }
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