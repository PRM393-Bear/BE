package com.example.PRM.service;

import com.example.PRM.dto.request.DonationEventFilterReq;
import com.example.PRM.dto.request.DonationEventReq;
import com.example.PRM.dto.response.DonationEventRes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface DonationEventService {
    public void createDonationEvent(DonationEventReq donationEventReq, UUID orgId,  HttpServletRequest request,UserDetails userDetails);
    public void updateDonationEvent(UUID donationEventId, DonationEventReq donationEventReq,UserDetails userDetails, HttpServletRequest request);
    public void deleteDonationEvent(UUID donationEventId,UserDetails userDetails, HttpServletRequest request);
    public List<DonationEventRes> getAllDonationEvents();
    public List<DonationEventRes> getAllByFilter(DonationEventFilterReq donationEventFilterReq);

}
