package com.example.PRM.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "action", length = 50, nullable = false)
    private String action;          // LOGIN, LOGOUT, CREATE, UPDATE, DELETE...

    @Column(name = "entity", length = 50)
    private String entity;          // USER, PRODUCT, WARDROBE, DONATION...

    @Column(name = "entity_id", length = 100)
    private String entityId;        // ID của object bị tác động

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;     // Mô tả chi tiết

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "status", length = 20)
    private String status;          // SUCCESS, FAILED

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }
}