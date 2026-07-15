package com.example.PRM.controller;

import com.example.PRM.dto.request.organizationDetail.OrganizationDetailReq;
import com.example.PRM.dto.response.org.OrganizationDetailRes;
import com.example.PRM.service.AuditLogService;
import com.example.PRM.service.OrganizationDetailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organization-details")
@RequiredArgsConstructor
public class OrganizationDetailController {

    private final OrganizationDetailService organizationDetailService;
    private final AuditLogService auditLogService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<String> createOrganizationDetail(
            @RequestBody OrganizationDetailReq request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        OrganizationDetailRes res = organizationDetailService.createOrganizationDetail(request, userDetails);

        auditLogService.log("CREATE_ORGANIZATION_DETAIL_SUCCESS",
                "ORGANIZATION_DETAIL",
                res.getId().toString(),
                "Organization detail created successfully",
                "SUCCESS",
                res.getUserId(),
                userDetails.getUsername(),
                httpRequest
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Organization detail created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateOrganizationDetail(
            @PathVariable UUID id,
            @RequestBody OrganizationDetailReq request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        OrganizationDetailRes res = organizationDetailService.updateOrganizationDetail(id, request);

        auditLogService.log("UPDATE_ORGANIZATION_DETAIL_SUCCESS",
                "ORGANIZATION_DETAIL",
                id.toString(),
                "Organization detail updated successfully",
                "SUCCESS",
                res.getUserId(),
                userDetails.getUsername(),
                httpRequest
        );

        return ResponseEntity.ok("Organization detail updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrganizationDetail(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        OrganizationDetailRes res = organizationDetailService.deleteOrganizationDetail(id, userDetails);

        auditLogService.log("DELETE_ORGANIZATION_DETAIL_SUCCESS",
                "ORGANIZATION_DETAIL",
                id.toString(),
                "Organization detail deleted successfully",
                "SUCCESS",
                res.getUserId(),
                userDetails.getUsername(),
                httpRequest
        );

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
    public ResponseEntity<String> approveOrganization(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        OrganizationDetailRes res = organizationDetailService.approveOrganization(id, userDetails);

        auditLogService.log("APPROVE_ORGANIZATION_SUCCESS",
                "ORGANIZATION_DETAIL",
                id.toString(),
                "Organization approved successfully",
                "SUCCESS",
                res.getUserId(),
                userDetails.getUsername(),
                httpRequest
        );

        return ResponseEntity.ok("Organization approved successfully");
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> rejectOrganization(
            @PathVariable UUID id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        OrganizationDetailRes res = organizationDetailService.rejectOrganization(id, userDetails, reason);

        auditLogService.log("REJECT_ORGANIZATION_SUCCESS",
                "ORGANIZATION_DETAIL",
                id.toString(),
                "Organization rejected successfully, reason: " + reason,
                "SUCCESS",
                res.getUserId(),
                userDetails.getUsername(),
                httpRequest
        );

        return ResponseEntity.ok("Organization rejected successfully");
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<OrganizationDetailRes>> getNearbyOrganizations(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(defaultValue = "50.0") double radius) {

        List<OrganizationDetailRes> response = organizationDetailService.getNearbyOrganizations(latitude, longitude, radius);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/my-profile/")
    public ResponseEntity<?> getMyOrganizationDetail(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(organizationDetailService.getOrganizationDetailByUserId(userDetails));
    }
}