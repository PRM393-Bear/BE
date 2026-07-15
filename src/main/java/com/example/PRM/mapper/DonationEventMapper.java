package com.example.PRM.mapper;

import com.example.PRM.dto.request.donationEvent.DonationEventReq;
import com.example.PRM.dto.response.donationEvent.DonationEventLogRes;
import com.example.PRM.dto.response.donationEvent.DonationEventRes;
import com.example.PRM.entity.DonationEvent;
import org.springframework.stereotype.Component;

@Component
public class DonationEventMapper {
    public DonationEventRes toResponse(DonationEvent donationEvent){
        DonationEventRes donationEventRes = new DonationEventRes();
        donationEventRes.setId(donationEvent.getId());
        donationEventRes.setTitle(donationEvent.getTitle());
        donationEventRes.setDescription(donationEvent.getDescription());
        donationEventRes.setAcceptedTypes(donationEvent.getAcceptedTypes());
        donationEventRes.setLocation(donationEvent.getLocation());
        donationEventRes.setLatitude(donationEvent.getLatitude());
        donationEventRes.setLongitude(donationEvent.getLongitude());
        donationEventRes.setEndDate(donationEvent.getEndDate());
        donationEventRes.setStartDate(donationEvent.getStartDate());
        donationEventRes.setBannerUrl(donationEvent.getBannerUrl());
        donationEventRes.setStatus(donationEvent.getStatus());
        donationEventRes.setTargetQuantity(donationEvent.getTargetQuantity());
        return donationEventRes;
    }

    public DonationEvent toEntity(DonationEventReq donationEventreq){
        DonationEvent donationEvent = new DonationEvent();

        donationEvent.setTitle(donationEventreq.getTitle());
        donationEvent.setDescription(donationEventreq.getDescription());
        donationEvent.setAcceptedTypes(donationEventreq.getAcceptedTypes());
        donationEvent.setLocation(donationEventreq.getLocation());
        donationEvent.setLatitude(donationEventreq.getLatitude());
        donationEvent.setLongitude(donationEventreq.getLongitude());
        donationEvent.setEndDate(donationEventreq.getEndDate());
        donationEvent.setStartDate(donationEventreq.getStartDate());
        donationEvent.setBannerUrl(donationEventreq.getBannerUrl());
        donationEvent.setTargetQuantity(donationEventreq.getTargetQuantity());
        donationEvent.setStatus(donationEventreq.getStatus());

        return donationEvent;
    }

    public DonationEventLogRes toResponseLog(DonationEvent donationEvent){
        DonationEventLogRes donationEventLogRes = new DonationEventLogRes();
        donationEventLogRes.setDonationEventId(donationEvent.getId());
        donationEventLogRes.setDonationEventName(donationEvent.getTitle());
        donationEventLogRes.setUserId(donationEvent.getOrganizationDetail().getUser().getUserId());
        donationEventLogRes.setUsername(donationEvent.getOrganizationDetail().getUser().getUserName());
        return donationEventLogRes;
    }

}
