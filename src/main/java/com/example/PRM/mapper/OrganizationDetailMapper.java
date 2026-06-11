package com.example.PRM.mapper;

import com.example.PRM.dto.request.OrganizationDetailReq;
import com.example.PRM.dto.response.OrganizationDetailRes;
import com.example.PRM.entity.OrganizationDetail;
import org.springframework.stereotype.Component;

@Component
public class OrganizationDetailMapper {
    public OrganizationDetailRes toResponse(OrganizationDetail od){
        OrganizationDetailRes res = new OrganizationDetailRes();
        res.setDescription(od.getDescription());
        res.setAddress(od.getAddress());
        res.setLatitude(od.getLatitude());
        res.setLongitude(od.getLongitude());
        res.setOrgName(od.getOrgName());
        return res;
    }

    public OrganizationDetail toEntity(OrganizationDetailReq req){
        OrganizationDetail od = new OrganizationDetail();
        od.setDescription(req.getDescription());
        od.setAddress(req.getAddress());
        od.setLatitude(req.getLatitude());
        od.setLongitude(req.getLongitude());
        od.setOrgName(req.getOrgName());
        return od;
    }
}
