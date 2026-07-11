package com.example.PRM.mapper;

import com.example.PRM.dto.request.organizationDetail.OrganizationDetailReq;
import com.example.PRM.dto.response.OrganizationDetailRes;
import com.example.PRM.entity.OrganizationDetail;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class OrganizationDetailMapper {
    public OrganizationDetailRes toResponse(OrganizationDetail od) {
        OrganizationDetailRes res = new OrganizationDetailRes();

        // Field cũ
        res.setOrgName(od.getOrgName());
        res.setDescription(od.getDescription());
        res.setAddress(od.getAddress());
        res.setLatitude(od.getLatitude());
        res.setLongitude(od.getLongitude());
        res.setAvtOrg(od.getAvtOrg());

        // Field mới từ OrganizationDetail
        res.setId(od.getId());
        res.setStatus(od.getStatus());
        res.setAcceptedTypes(od.getAcceptedTypes() != null ? od.getAcceptedTypes() : new ArrayList<>());
        res.setVerificationDocs(od.getVerificationDocs() != null ? od.getVerificationDocs() : new ArrayList<>());
        res.setTotalDonationReceived(od.getTotalDonationReceived());

        // Field từ User (cần check null phòng trường hợp user bị xóa)
        if (od.getUser() != null) {
            res.setUserId(od.getUser().getUserId());
            res.setUserFullName(od.getUser().getFullName());
            res.setUserEmail(od.getUser().getEmail());
        }

        return res;
    }

    public OrganizationDetail toEntity(OrganizationDetailReq req){
        OrganizationDetail od = new OrganizationDetail();
        od.setDescription(req.getDescription());
        od.setAddress(req.getAddress());
        od.setLatitude(req.getLatitude());
        od.setLongitude(req.getLongitude());
        od.setOrgName(req.getOrgName());
        od.setAvtOrg(req.getAvtOrg());
        od.setAcceptedTypes(req.getAcceptedTypes());     // ✅ thêm
        od.setVerificationDocs(req.getVerificationDocs()); // ✅ thêm
        return od;
    }
}
