package com.example.PRM.controller;

import com.example.PRM.dto.request.*;
import com.example.PRM.dto.response.DonationPendingResponse;
import com.example.PRM.service.DonationRequestService;
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

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody DonationRequestReq request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request1) {

        userDetails.getAuthorities();
        donationRequestService.createDonationRequest(request, userDetails,request1);

        return ResponseEntity.ok("Donation request created successfully");
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping(value = "/custom", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCustom(
            @ModelAttribute DonationRequestCustomReq request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request1) {

        donationRequestService.createDonationRequest(request, userDetails,request1);
        return ResponseEntity.ok("Donation request created successfully");
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PatchMapping("/{id}/accept")
    public ResponseEntity<?> accept(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request1) {

        donationRequestService.accept(id, userDetails,request1);

        return ResponseEntity.ok("Donation request accepted successfully");
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(
            @PathVariable UUID id,
            @RequestBody DonationRequestReject request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request1) {

        donationRequestService.reject(id, request.getReason(),userDetails,request1);

        return ResponseEntity.ok("Donation request rejected successfully");
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PatchMapping("/{id}/shipping")
    public ResponseEntity<?> shipping(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request1) {

        donationRequestService.shipping(id, userDetails,request1);

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
        donationRequestService.shipped(id, req, userDetails,request1);
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
        donationRequestService.received(id, req, userDetails,request1);
        return ResponseEntity.ok("Received donation request successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/completed")
    public ResponseEntity<Void> completed(
            @PathVariable UUID id,
            HttpServletRequest request1) {

        donationRequestService.completed(id, request1);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(
            @PathVariable UUID id,
            @RequestBody String reason,
            @AuthenticationPrincipal UserDetails userDetails){
        donationRequestService.cancel(id, reason, userDetails);
        return ResponseEntity.ok("Cancel donation request successfully");
    }


    @PatchMapping("/{donationId}/assign-organization/{organizationId}")
    public ResponseEntity<?> assignOrganization(
            @PathVariable UUID donationId,
            @PathVariable UUID organizationId,
            HttpServletRequest request1
    ) {

        donationRequestService.assignOrganization(donationId, organizationId, request1);

        return ResponseEntity.ok(
                "Assign organization successfully"
        );
    }

    @GetMapping("/lists")
    public ResponseEntity<?> getDonationRequestsPending(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<DonationPendingResponse> lists = donationRequestService.getPendingDonations(userDetails);
        return ResponseEntity.ok(lists);
    }
}
