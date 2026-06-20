package com.example.PRM.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationEventFilterReq {
    private String itemType;

    private String city;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Double maxDistanceKm;
}
