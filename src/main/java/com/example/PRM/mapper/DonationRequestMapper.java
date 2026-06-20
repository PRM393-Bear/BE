package com.example.PRM.mapper;

import com.example.PRM.dto.request.DonationRequestReq;
import com.example.PRM.dto.response.DonationPendingResponse;
import com.example.PRM.entity.DonationRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DonationRequestMapper {
    public DonationRequest toEntity(DonationRequestReq donationRequestReq){
        DonationRequest donationRequest = new DonationRequest();

        donationRequest.getImages().add(donationRequestReq.getImageUrl());

        donationRequest.setDescription(donationRequestReq.getDescription());

        donationRequest.setTrackingCode(donationRequestReq.getTrackingCode());

        return donationRequest;
    }

    public DonationPendingResponse toPendingResponse(DonationRequest donationRequest){
        DonationPendingResponse donationPendingResponse = new DonationPendingResponse();
        donationPendingResponse.setTrackingCode(donationRequest.getTrackingCode());
        donationPendingResponse.setCreatedAt(donationRequest.getCreatedAt());
        donationPendingResponse.setDescription(donationRequest.getDescription());
        donationPendingResponse.setId(donationRequest.getId());
        donationPendingResponse.setUsername(donationRequest.getUser().getUserName());
        if(donationRequest.getDonationEvent() != null) {
            donationPendingResponse.setEventName(donationRequest.getDonationEvent().getTitle());
        }else {
            donationPendingResponse.setEventName(null);
        }
        donationPendingResponse.setOrganizationName(donationRequest.getOrganizationDetail().getOrgName());
        return donationPendingResponse;
    }

}
