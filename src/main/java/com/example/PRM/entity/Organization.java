package com.example.PRM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "organization_detail")
@Getter
@Setter
@NoArgsConstructor
public class Organization {

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

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 8)
    private BigDecimal longitude;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> acceptedTypes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> verificationDocs;

    private Boolean isVerified = false;

    private Integer totalDonationReceived = 0;
}

