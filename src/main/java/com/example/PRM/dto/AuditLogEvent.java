package com.example.PRM.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEvent {
    private UUID userId;
    private String username;
    private String action;
    private String entity;
    private String entityId;
    private String description;
    private String ipAddress;
    private String userAgent;
    private String status;
}