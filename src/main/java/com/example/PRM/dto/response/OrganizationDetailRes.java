package com.example.PRM.dto.response;

import com.example.PRM.status_enum.VerificationOrganizationStatus;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationDetailRes {
    private UUID id;

    // Thông tin org
    private String orgName;
    private String description;
    private String address;
    private String websiteUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String avtOrg;
    private List<String> acceptedTypes;
    private List<String> verificationDocs;
    private Integer totalDonationReceived;
    private VerificationOrganizationStatus status;

    // Thông tin user owner
    private UUID userId;
    private String userFullName;
    private String userEmail;
    private String reason;
}
