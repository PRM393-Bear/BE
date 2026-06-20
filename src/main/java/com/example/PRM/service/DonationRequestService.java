package com.example.PRM.service;

import com.example.PRM.dto.request.DonationRequestReq;
import com.example.PRM.dto.response.DonationPendingResponse;
import com.example.PRM.entity.DonationRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface DonationRequestService {
    public void createDonationRequest(DonationRequestReq donationRequestReq, UserDetails userDetails);

    public void accept(UUID donationRequestId);

    public void reject(UUID donationRequestId, String reason);

    public void shipping(UUID donationRequestId);

    public void shipped(UUID donationRequestId);

    public void received(UUID donationRequestId);

    public void completed(UUID donationRequestId);

    public void cancel(UUID donationRequestId, String cancelReason);

    public void checkPendingDonations();

    public void assignOrganization(UUID donationRequestId, UUID organizationId);

    public List<DonationPendingResponse> getPendingDonations(UserDetails userDetails);
}
