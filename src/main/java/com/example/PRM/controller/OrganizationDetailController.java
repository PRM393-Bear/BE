package com.example.PRM.controller;

import com.example.PRM.dto.request.OrganizationDetailReq;
import com.example.PRM.dto.response.OrganizationDetailRes;
import com.example.PRM.service.OrganizationDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/organization-details")
@RequiredArgsConstructor
public class OrganizationDetailController {

    private final OrganizationDetailService organizationDetailService;

    @PostMapping
    public ResponseEntity<String> createOrganizationDetail(
            @RequestBody OrganizationDetailReq request, @AuthenticationPrincipal UserDetails userDetails) {

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
}