package com.example.PRM.mapper;

import com.example.PRM.dto.request.donationRequest.DonationRequestCustomReq;
import com.example.PRM.dto.request.donationRequest.DonationRequestReq;
import com.example.PRM.dto.response.DonationPendingResponse;
import com.example.PRM.entity.DonationRequest;
import com.example.PRM.status_enum.DonationStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DonationRequestMapper {
    public DonationRequest toEntity(DonationRequestReq donationRequestReq){
        DonationRequest donationRequest = new DonationRequest();

        donationRequest.setDescription(donationRequestReq.getDescription());

        return donationRequest;
    }

    public DonationRequest toEntity(DonationRequestCustomReq donationRequestReq){
        DonationRequest donationRequest = new DonationRequest();

        donationRequest.setDescription(donationRequestReq.getDescription());
        donationRequest.setCreatedAt(LocalDateTime.now());
        donationRequest.setStatus(DonationStatus.PENDING);

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
