package com.example.PRM.service;

import com.example.PRM.dto.request.organizationDetail.OrganizationDetailReq;
import com.example.PRM.dto.response.OrganizationDetailRes;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface OrganizationDetailService {
    void createOrganizationDetail(OrganizationDetailReq organizationDetailReq, UserDetails userDetails);
    void updateOrganizationDetail(UUID organizationDetailId, OrganizationDetailReq organizationDetailReq);
    void deleteOrganizationDetail(UUID organizationId, UserDetails userDetails);
    OrganizationDetailRes getOrganizationDetail(UUID organizationId);

    List<OrganizationDetailRes> getAllOrganizations();
    List<OrganizationDetailRes> getPendingOrganizations();
    void approveOrganization(UUID organizationId, UserDetails userDetails);
    void rejectOrganization(UUID organizationId, UserDetails userDetails, String reason);

}
