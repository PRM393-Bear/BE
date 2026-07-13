package com.example.PRM.dto.request.donationRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingReq {
    private String trackingCode;
    private MultipartFile shippingProofFile;
}
