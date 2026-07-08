package com.example.PRM.dto.response.donationEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationEventLogRes {
    private String donationEventName;
    private String username;
    private UUID donationEventId;
    private UUID userId;
}
