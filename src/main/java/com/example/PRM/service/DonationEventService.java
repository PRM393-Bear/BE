package com.example.PRM.service;

import com.example.PRM.dto.request.donationEvent.DonationEventFilterReq;
import com.example.PRM.dto.request.donationEvent.DonationEventReq;
import com.example.PRM.dto.response.donationEvent.DonationEventLogRes;
import com.example.PRM.dto.response.donationEvent.DonationEventRes;
import com.example.PRM.status_enum.EventStatus;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface DonationEventService {
    public DonationEventLogRes createDonationEvent(DonationEventReq donationEventReq, UUID orgId, UserDetails userDetails);
    public DonationEventLogRes updateDonationEvent(UUID donationEventId, DonationEventReq donationEventReq,UserDetails userDetails);
    public DonationEventLogRes deleteDonationEvent(UUID donationEventId,UserDetails userDetails);
    public List<DonationEventRes> getAllDonationEvents();
    public List<DonationEventRes> getAllByFilter(DonationEventFilterReq donationEventFilterReq);
    public List<DonationEventRes> getAllByOrgId(UUID orgId);
    public DonationEventLogRes cancelDonationEvent(UUID donationEventId, UserDetails userDetails);
    public DonationEventLogRes completeDonationEvent(UUID donationEventId, UserDetails userDetails);
    public DonationEventLogRes ongoingDonationEvent(UUID donationEventId, UserDetails userDetails);


}
