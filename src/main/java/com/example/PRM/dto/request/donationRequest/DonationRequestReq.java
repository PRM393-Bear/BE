package com.example.PRM.dto.request.donationRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationRequestReq {
    private UUID donationEventId;

    private String description;

    private UUID wardrobeItemId;
}
