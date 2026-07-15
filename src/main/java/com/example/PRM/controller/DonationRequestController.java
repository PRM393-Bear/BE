package com.example.PRM.controller;

import com.example.PRM.dto.request.donationRequest.*;
import com.example.PRM.dto.response.DonationPendingResponse;
import com.example.PRM.entity.DonationRequest;
import com.example.PRM.service.DonationRequestService;
import com.example.PRM.serviceImpl.AuditLogServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/donation-requests")
@RequiredArgsConstructor
public class DonationRequestController {

    private final DonationRequestService donationRequestService;
    private final AuditLogServiceImpl auditLogService;
    private static final String ENTITY = "DonationRequest";

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody DonationRequestReq request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request1) {

        DonationRequest donationRequest = donationRequestService.createDonationRequest(request, userDetails);

        auditLogService.log(
                "CREATE_DONATION_REQUEST",
                ENTITY,
                donationRequest.getId().toString(),
                "User created donation request successfully",
                "SUCCESS",
                donationRequest.getUser().getUserId(),
                donationRequest.getUser().getUserName(),
                request1
        );

        return ResponseEntity.ok("Donation request created successfully");
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping(value = "/custom", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCustom(
            @ModelAttribute DonationRequestCustomReq request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request1) {

        DonationRequest donationRequest = donationRequestService.createDonationRequest(request, userDetails);

        auditLogService.log(
                "CREATE_DONATION_REQUEST_CUSTOM",
                ENTITY,
                donationRequest.getId().toString(),
                "User created custom donation request successfully",
                "SUCCESS",
                donationRequest.getUser().getUserId(),
                donationRequest.getUser().getUserName(),
                request1
        );

        return ResponseEntity.ok("Donation request created successfully");
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PatchMapping("/{id}/accept")
    public ResponseEntity<?> accept(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request1) {

        DonationRequest donationRequest = donationRequestService.accept(id, userDetails);

        auditLogService.log(
                "ACCEPT_DONATION_REQUEST",
                ENTITY,
                id.toString(),
                "Organization accepted donation request",
                "SUCCESS",
                donationRequest.getOrganizationDetail().getUser().getUserId(),
                donationRequest.getOrganizationDetail().getUser().getUserName(),
                request1
        );

        return ResponseEntity.ok("Donation request accepted successfully");
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(
            @PathVariable UUID id,
            @RequestBody DonationRequestReject request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request1) {

        DonationRequest donationRequest = donationRequestService.reject(id, request.getReason(), userDetails);

        auditLogService.log(
                "REJECT_DONATION_REQUEST",
                ENTITY,
                id.toString(),
                "Organization rejected donation request. Reason: " + request.getReason(),
                "SUCCESS",
                donationRequest.getOrganizationDetail().getUser().getUserId(),
                donationRequest.getOrganizationDetail().getUser().getUserName(),
                request1
        );

        return ResponseEntity.ok("Donation request rejected successfully");
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PatchMapping("/{id}/shipping")
    public ResponseEntity<?> shipping(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request1) {

        DonationRequest donationRequest = donationRequestService.shipping(id, userDetails);

        auditLogService.log(
                "USER_SHIPPING",
                ENTITY,
                id.toString(),
                "User changed status to SHIPPING",
                "SUCCESS",
                donationRequest.getUser().getUserId(),
                donationRequest.getUser().getUserName(),
                request1
        );

        return ResponseEntity.ok("Shipping donation request successfully");
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PatchMapping(value = "/{id}/shipped", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> shipped(
            @PathVariable UUID id,
            @RequestParam("trackingCode") String trackingCode,
            @RequestParam("shippingProofFile") MultipartFile shippingProofFile,
            HttpServletRequest request1,
            @AuthenticationPrincipal UserDetails userDetails) {

        ShippingReq req = new ShippingReq(trackingCode, shippingProofFile);
        DonationRequest donationRequest = donationRequestService.shipped(id, req, userDetails);

        auditLogService.log(
                "IMAGE_PROVE_SHIPPED",
                ENTITY,
                id.toString(),
                "User uploaded shipping proof image",
                "SUCCESS",
                donationRequest.getUser().getUserId(),
                donationRequest.getUser().getUserName(),
                request1
        );

        return ResponseEntity.ok("Shipped donation request successfully");
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PatchMapping(value = "/{id}/received", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> received(
            @PathVariable UUID id,
            @RequestParam("receiptProofFile") MultipartFile receiptProofFile,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request1) {

        ReceivedReq req = new ReceivedReq(receiptProofFile);
        DonationRequest donationRequest = donationRequestService.received(id, req, userDetails);

        auditLogService.log(
                "IMAGE_PROVE_RECEIVED",
                ENTITY,
                id.toString(),
                "Organization uploaded receipt proof image",
                "SUCCESS",
                donationRequest.getOrganizationDetail().getUser().getUserId(),
                donationRequest.getOrganizationDetail().getUser().getUserName(),
                request1
        );

        return ResponseEntity.ok("Received donation request successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/completed")
    public ResponseEntity<Void> completed(
            @PathVariable UUID id,
            HttpServletRequest request1) {

        DonationRequest donationRequest = donationRequestService.completed(id);

        auditLogService.log(
                "COMPLETED_DONATION",
                ENTITY,
                id.toString(),
                "Donation request completed",
                "SUCCESS",
                donationRequest.getUser().getUserId(),
                donationRequest.getUser().getUserName(),
                request1
        );

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(
            @PathVariable UUID id,
            @RequestBody String reason,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request1) {

        DonationRequest donationRequest = donationRequestService.cancel(id, reason, userDetails);

        auditLogService.log(
                "CANCEL_DONATION_REQUEST",
                ENTITY,
                id.toString(),
                "User cancelled donation request. Reason: " + reason,
                "SUCCESS",
                donationRequest.getUser().getUserId(),
                donationRequest.getUser().getUserName(),
                request1
        );

        return ResponseEntity.ok("Cancel donation request successfully");
    }

    @PatchMapping("/{donationId}/assign-organization/{organizationId}")
    public ResponseEntity<?> assignOrganization(
            @PathVariable UUID donationId,
            @PathVariable UUID organizationId,
            HttpServletRequest request1
    ) {

        donationRequestService.assignOrganization(donationId, organizationId);

        auditLogService.log(
                "ASSIGN_ORGANIZATION",
                ENTITY,
                donationId.toString(),
                "Admin assigned organization " + organizationId + " to donation request",
                "SUCCESS",
                null,
                "SYSTEM",
                request1
        );

        return ResponseEntity.ok("Assign organization successfully");
    }

    @GetMapping("/lists")
    public ResponseEntity<?> getDonationRequestsPending(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<DonationPendingResponse> lists = donationRequestService.getPendingDonations(userDetails);
        return ResponseEntity.ok(lists);
    }

    @GetMapping("/my-organization/{orgId}")
    public ResponseEntity<?> getDonations(@PathVariable UUID orgId) {
        return ResponseEntity.ok(donationRequestService.getAllDonationRequestsFromOrganizationId(orgId));
    }

    @GetMapping("/my-member")
    public ResponseEntity<?> getMember(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(donationRequestService.getAllDonationRequestsFromUser(userDetails));
    }
}