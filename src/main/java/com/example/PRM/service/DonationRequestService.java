package com.example.PRM.service;

import com.example.PRM.dto.request.DonationRequestCustomReq;
import com.example.PRM.dto.request.DonationRequestReq;
import com.example.PRM.dto.request.ReceivedReq;
import com.example.PRM.dto.request.ShippingReq;
import com.example.PRM.dto.response.DonationPendingResponse;
import com.example.PRM.entity.DonationRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface DonationRequestService {
    void createDonationRequest(DonationRequestReq donationRequestReq, UserDetails userDetails,HttpServletRequest request);
    void createDonationRequest(DonationRequestCustomReq donationRequestReq, UserDetails userDetails, HttpServletRequest request);

    void accept(UUID donationRequestId, UserDetails userDetails, HttpServletRequest request);
    void reject(UUID donationRequestId, String reason, UserDetails userDetails, HttpServletRequest request);

    void shipping(UUID donationRequestId, UserDetails userDetails, HttpServletRequest request);
    void shipped(UUID donationRequestId, ShippingReq req, UserDetails userDetails,HttpServletRequest request);
    void received(UUID donationRequestId, ReceivedReq req, UserDetails userDetails,HttpServletRequest request);

    void completed(UUID donationRequestId, HttpServletRequest request);

    void cancel(UUID donationRequestId, String cancelReason, UserDetails userDetails); // ✅ thêm userDetails

    void checkPendingDonations();
    void assignOrganization(UUID donationRequestId, UUID organizationId,HttpServletRequest request);

    List<DonationPendingResponse> getPendingDonations(UserDetails userDetails);
}
