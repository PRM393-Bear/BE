package com.example.PRM.dto.response.donationRequest;

import com.example.PRM.status_enum.DonationStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationRequestResponse {
    private UUID id;

    private String username;

    private String organizationName;

    private String eventName;

    private String trackingCode;

    private String description;

    private LocalDateTime createdAt;

    private long daysPending;

    private List<String> images = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private DonationStatus status;
}
