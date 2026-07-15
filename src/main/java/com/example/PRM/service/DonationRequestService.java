package com.example.PRM.service;

import com.example.PRM.dto.request.donationRequest.DonationRequestCustomReq;
import com.example.PRM.dto.request.donationRequest.DonationRequestReq;
import com.example.PRM.dto.request.donationRequest.ReceivedReq;
import com.example.PRM.dto.request.donationRequest.ShippingReq;
import com.example.PRM.dto.response.DonationPendingResponse;
import com.example.PRM.dto.response.donationRequest.DonationRequestResponse;
import com.example.PRM.entity.DonationRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface DonationRequestService {

    DonationRequest createDonationRequest(DonationRequestReq donationRequestReq, UserDetails userDetails);

    DonationRequest createDonationRequest(DonationRequestCustomReq donationRequestReq, UserDetails userDetails);

    DonationRequest accept(UUID donationRequestId, UserDetails userDetails);

    DonationRequest reject(UUID donationRequestId, String reason, UserDetails userDetails);

    DonationRequest shipping(UUID donationRequestId, UserDetails userDetails);

    DonationRequest shipped(UUID donationRequestId, ShippingReq req, UserDetails userDetails);

    DonationRequest received(UUID donationRequestId, ReceivedReq req, UserDetails userDetails);

    DonationRequest completed(UUID donationRequestId);

    DonationRequest cancel(UUID donationRequestId, String cancelReason, UserDetails userDetails);

    void checkPendingDonations();

    DonationRequest assignOrganization(UUID donationRequestId, UUID organizationId);

    List<DonationPendingResponse> getPendingDonations(UserDetails userDetails);

    List<DonationRequestResponse> getAllDonationRequestsFromOrganizationId(UUID organizationId);

    List<DonationRequestResponse> getAllDonationRequestsFromUser(UserDetails userDetails);

    void autoCheckReceivedDonations();

}