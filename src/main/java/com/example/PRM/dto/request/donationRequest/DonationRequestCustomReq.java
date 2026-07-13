package com.example.PRM.dto.request.donationRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationRequestCustomReq {
    private UUID donationEventId;
    private String description;

    // WardrobeItem mới
    private String itemName;
    private String category;
    private String condition;
    private String conditionNote;
    private MultipartFile image;
}
