package com.example.PRM.entity;

import com.example.PRM.status_enum.VerificationOrganizationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "organization_detail")
@Getter
@Setter
@NoArgsConstructor
public class OrganizationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(length = 150)
    private String orgName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(precision = 15, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 15, scale = 8)
    private BigDecimal longitude;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> acceptedTypes = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> verificationDocs;

    @Enumerated(EnumType.STRING)
    private VerificationOrganizationStatus status = VerificationOrganizationStatus.PENDING;

    private Integer totalDonationReceived = 0;

    @OneToMany(mappedBy = "organization")
    private List<Order> orders = new ArrayList<>();

    private String avtOrg;
}

