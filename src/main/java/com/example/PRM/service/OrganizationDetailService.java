package com.example.PRM.service;

import com.example.PRM.dto.request.organizationDetail.OrganizationDetailReq;
import com.example.PRM.dto.response.org.OrganizationDetailRes;
import org.springframework.security.core.userdetails.UserDetails;
import java.math.BigDecimal;

import java.util.List;
import java.util.UUID;

public interface OrganizationDetailService {
    OrganizationDetailRes createOrganizationDetail(OrganizationDetailReq organizationDetailReq, UserDetails userDetails);
    OrganizationDetailRes updateOrganizationDetail(UUID organizationDetailId, OrganizationDetailReq organizationDetailReq, UserDetails userDetails);
    OrganizationDetailRes deleteOrganizationDetail(UUID organizationId, UserDetails userDetails);
    OrganizationDetailRes getOrganizationDetail(UUID organizationId);
    List<OrganizationDetailRes> getAllOrganizations();
    List<OrganizationDetailRes> getPendingOrganizations();
    OrganizationDetailRes approveOrganization(UUID organizationId, UserDetails userDetails);
    OrganizationDetailRes rejectOrganization(UUID organizationId, UserDetails userDetails, String reason);
    List<OrganizationDetailRes> getNearbyOrganizations(BigDecimal latitude, BigDecimal longitude, double radius);
    OrganizationDetailRes getOrganizationDetailByUserId(UserDetails userDetails);
}
