package com.example.PRM.entity;
import com.example.PRM.status_enum.DonationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "donation_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_detail_id", nullable = false)
    private OrganizationDetail organizationDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_event_id")
    private DonationEvent donationEvent;

    @OneToMany(mappedBy = "donationRequest")
    private List<WardrobeItem> items = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> images = new ArrayList<>();

    @Column(length = 50)
    private String trackingCode;

    @Column(columnDefinition = "TEXT")
    private String shippingProofUrl;

    @Column(columnDefinition = "TEXT")
    private String receiptProofUrl;

    @Enumerated(EnumType.STRING)
    private DonationStatus status;

    @Column(columnDefinition = "TEXT")
    private String rejectedReason;

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    private LocalDateTime canceledAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime shippedAt;

    private LocalDateTime completedAt;

    private LocalDateTime ReminderSentAt;

}
