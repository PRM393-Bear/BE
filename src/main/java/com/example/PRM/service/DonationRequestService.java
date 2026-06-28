package com.example.PRM.service;

import com.example.PRM.dto.request.DonationRequestCustomReq;
import com.example.PRM.dto.request.DonationRequestReq;
import com.example.PRM.dto.request.ReceivedReq;
import com.example.PRM.dto.request.ShippingReq;
import com.example.PRM.dto.response.DonationPendingResponse;
import com.example.PRM.entity.DonationRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface DonationRequestService {
    void createDonationRequest(DonationRequestReq donationRequestReq, UserDetails userDetails);
    void createDonationRequest(DonationRequestCustomReq donationRequestReq, UserDetails userDetails);

    void accept(UUID donationRequestId, UserDetails userDetails);
    void reject(UUID donationRequestId, String reason, UserDetails userDetails);

    void shipping(UUID donationRequestId, UserDetails userDetails);
    void shipped(UUID donationRequestId, ShippingReq req, UserDetails userDetails);
    void received(UUID donationRequestId, ReceivedReq req, UserDetails userDetails);

    void completed(UUID donationRequestId);

    void cancel(UUID donationRequestId, String cancelReason, UserDetails userDetails); // ✅ thêm userDetails

    void checkPendingDonations();
    void assignOrganization(UUID donationRequestId, UUID organizationId);

    List<DonationPendingResponse> getPendingDonations(UserDetails userDetails);
}
