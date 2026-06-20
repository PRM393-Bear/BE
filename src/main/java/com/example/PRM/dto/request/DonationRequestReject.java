package com.example.PRM.dto.request;

import com.example.PRM.status_enum.DonationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationRequestReject {
    private String reason;
}
