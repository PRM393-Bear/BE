package com.example.PRM.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationRequestReq {
    private String donationEventName;

    private String description;

    private String imageUrl;

    private String organizationName;

    private String itemName;

    private String trackingCode;
}
