package com.example.PRM.controller;

import com.example.PRM.dto.request.DonationEventFilterReq;
import com.example.PRM.dto.request.DonationEventReq;
import com.example.PRM.service.DonationEventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/donation-events")
@RequiredArgsConstructor
public class DonationEventController {

    private final DonationEventService donationEventService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<?> createDonationEvent(
            @RequestBody DonationEventReq donationEventReq,
            @RequestParam UUID orgId,
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        donationEventService.createDonationEvent(
                donationEventReq,
                orgId,
                request,
                userDetails
        );

        return ResponseEntity.ok("Donation event created successfully");
    }

    @PutMapping("/{donationEventId}")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<?> updateDonationEvent(
            @PathVariable UUID donationEventId,
            @RequestBody DonationEventReq donationEventReq,
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        donationEventService.updateDonationEvent(
                donationEventId,
                donationEventReq,
                userDetails,
                request
        );

        return ResponseEntity.ok("Donation event updated successfully");
    }

    @GetMapping
    public ResponseEntity<?> getAllDonationEvents() {

        return ResponseEntity.ok(
                donationEventService.getAllDonationEvents()
        );
    }

    @PostMapping("/filter")
    public ResponseEntity<?> getAllByFilter(
            @RequestBody DonationEventFilterReq req
    ) {

        return ResponseEntity.ok(
                donationEventService.getAllByFilter(req)
        );
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<?> deleteDonationEvent(
            @RequestParam UUID donationEventId,
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        donationEventService.deleteDonationEvent(donationEventId, userDetails, request);
        return ResponseEntity.ok("Donation event deleted successfully");
    }
}
