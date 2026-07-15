package com.example.PRM.dto.request.donationRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationRequestReq {
    private UUID donationEventId;

    private String description;

    private List<UUID> wardrobeItemIds;
}
