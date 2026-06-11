package com.example.PRM.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_items_id")
    private WardrobeItem wardrobeItem;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> images;

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

    public enum DonationStatus {
        PENDING, ACCEPTED, REJECTED, SHIPPING, SHIPPED, RECEIVED, COMPLETED
    }
}
