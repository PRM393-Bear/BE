package com.example.PRM.service;

import com.example.PRM.dto.AuditLogEvent;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

public interface AuditLogService {
    void log(String action, String entity, String entityId,
             String description, String status,
             UUID userId, String username,
             HttpServletRequest request);
}