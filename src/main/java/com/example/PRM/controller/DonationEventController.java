package com.example.PRM.controller;

import com.example.PRM.dto.request.donationEvent.DonationEventFilterReq;
import com.example.PRM.dto.request.donationEvent.DonationEventReq;
import com.example.PRM.dto.response.donationEvent.DonationEventLogRes;
import com.example.PRM.service.AuditLogService;
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
    private final AuditLogService auditLogService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<?> createDonationEvent(
            @RequestBody DonationEventReq donationEventReq,
            @RequestParam UUID orgId,
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        DonationEventLogRes res = donationEventService.createDonationEvent(
                donationEventReq,
                orgId,
                userDetails
        );
        auditLogService.log(
                "CREATE_DONATION_EVENT",
                "DonationEvent",
                res.getDonationEventId().toString(),
                "Organization created donation event: " + res.getDonationEventName(),
                "SUCCESS",
                res.getUserId(),
                res.getUsername(),
                request
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

        DonationEventLogRes res = donationEventService.updateDonationEvent(
                donationEventId,
                donationEventReq,
                userDetails
        );
        auditLogService.log(
                "UPDATE_DONATION_EVENT",
                "DonationEvent",
                donationEventId.toString(),
                "Organization updated donation event: " + res.getDonationEventName(),
                "SUCCESS",
                res.getUserId(),
                res.getUsername(),
                request
        );

        return ResponseEntity.ok("Donation event updated successfully");
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<?> getDonationEvent(
            @PathVariable UUID orgId
    ) {
        return ResponseEntity.ok(donationEventService.getAllByOrgId(orgId));
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
        DonationEventLogRes res = donationEventService.deleteDonationEvent(donationEventId, userDetails);
        auditLogService.log(
                "DELETE_DONATION_EVENT",
                "DonationEvent",
                donationEventId.toString(),
                "Organization deleted donation event: " + res.getDonationEventName(),
                "SUCCESS",
                res.getUserId(),
                res.getUsername(),
                request
        );
        return ResponseEntity.ok("Donation event deleted successfully");
    }
}
