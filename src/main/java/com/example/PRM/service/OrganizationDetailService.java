package com.example.PRM.service;

import com.example.PRM.dto.request.OrganizationDetailReq;
import com.example.PRM.dto.response.OrganizationDetailRes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface OrganizationDetailService {
    void createOrganizationDetail(OrganizationDetailReq organizationDetailReq, UserDetails userDetails);
    void updateOrganizationDetail(UUID organizationDetailId, OrganizationDetailReq organizationDetailReq);
    void deleteOrganizationDetail(UUID organizationId, UserDetails userDetails);
    OrganizationDetailRes getOrganizationDetail(UUID organizationId);

}
