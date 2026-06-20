package com.example.PRM.service;

import com.example.PRM.dto.request.DonationEventFilterReq;
import com.example.PRM.dto.request.DonationEventReq;
import com.example.PRM.dto.response.DonationEventRes;

import java.util.List;
import java.util.UUID;

public interface DonationEventService {
    public void createDonationEvent(DonationEventReq donationEventReq, String orgName);
    public void updateDonationEvent(UUID donationEventId, DonationEventReq donationEventReq);
    public List<DonationEventRes> getAllDonationEvents();
    public List<DonationEventRes> getAllByFilter(DonationEventFilterReq donationEventFilterReq);

}
