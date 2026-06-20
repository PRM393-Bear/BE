package com.example.PRM.controller;

import com.example.PRM.dto.request.DonationRequestReject;
import com.example.PRM.dto.request.DonationRequestReq;
import com.example.PRM.dto.response.DonationPendingResponse;
import com.example.PRM.service.DonationRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/donation-requests")
@RequiredArgsConstructor
public class DonationRequestController {

    private final DonationRequestService donationRequestService;

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody DonationRequestReq request, @AuthenticationPrincipal UserDetails userDetails) {

        userDetails.getAuthorities();
        donationRequestService.createDonationRequest(request, userDetails);

        return ResponseEntity.ok("Donation request created successfully");
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PatchMapping("/{id}/accept")
    public ResponseEntity<?> accept(
            @PathVariable UUID id) {

        donationRequestService.accept(id);

        return ResponseEntity.ok("Donation request accepted successfully");
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(
            @PathVariable UUID id,
            @RequestBody DonationRequestReject request) {

        donationRequestService.reject(id, request.getReason());

        return ResponseEntity.ok("Donation request rejected successfully");
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PatchMapping("/{id}/shipping")
    public ResponseEntity<?> shipping(
            @PathVariable UUID id) {

        donationRequestService.shipping(id);

        return ResponseEntity.ok("Shipping donation request successfully");
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PatchMapping("/{id}/shipped")
    public ResponseEntity<?> shipped(
            @PathVariable UUID id) {

        donationRequestService.shipped(id);

        return ResponseEntity.ok("Shipped donation request successfully");
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PatchMapping("/{id}/received")
    public ResponseEntity<?> received(
            @PathVariable UUID id) {

        donationRequestService.received(id);

        return ResponseEntity.ok("Received donation request successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/completed")
    public ResponseEntity<Void> completed(
            @PathVariable UUID id) {

        donationRequestService.completed(id);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable UUID id,@RequestBody String reason){
        donationRequestService.cancel(id, reason);
        return ResponseEntity.ok("Cancel donation request successfully");
    }


    @PatchMapping("/{donationId}/assign-organization/{organizationId}")
    public ResponseEntity<?> assignOrganization(
            @PathVariable UUID donationId,
            @PathVariable UUID organizationId
    ) {

        donationRequestService.assignOrganization(donationId, organizationId);

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
