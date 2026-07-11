package com.example.PRM.controller;

import com.example.PRM.dto.request.organizationDetail.OrganizationDetailReq;
import com.example.PRM.dto.response.OrganizationDetailRes;
import com.example.PRM.service.OrganizationDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organization-details")
@RequiredArgsConstructor
public class OrganizationDetailController {

    private final OrganizationDetailService organizationDetailService;

    @PostMapping
    public ResponseEntity<String> createOrganizationDetail(
            @RequestBody OrganizationDetailReq request,
            @AuthenticationPrincipal UserDetails userDetails) {

        organizationDetailService.createOrganizationDetail(request, userDetails);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Organization detail created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateOrganizationDetail(
            @PathVariable UUID id,
            @RequestBody OrganizationDetailReq request) {

        organizationDetailService.updateOrganizationDetail(id, request);

        return ResponseEntity.ok("Organization detail updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrganizationDetail(
            @PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {

        organizationDetailService.deleteOrganizationDetail(id, userDetails);

        return ResponseEntity.ok("Organization detail deleted successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationDetailRes> getOrganizationDetail(
            @PathVariable UUID id) {

        OrganizationDetailRes response =
                organizationDetailService.getOrganizationDetail(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrganizationDetailRes>> getAllOrganizations() {
        return ResponseEntity.ok(organizationDetailService.getAllOrganizations());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<OrganizationDetailRes>> getPendingOrganizations() {
        return ResponseEntity.ok(organizationDetailService.getPendingOrganizations());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> approveOrganization(@PathVariable UUID id,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        organizationDetailService.approveOrganization(id,userDetails);
        return ResponseEntity.ok("Organization approved successfully");
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> rejectOrganization(@PathVariable UUID id,
                                                     @RequestParam String reason,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        organizationDetailService.rejectOrganization(id,userDetails,reason);
        return ResponseEntity.ok("Organization rejected successfully");
    }
}