package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.OrganizationDetailReq;
import com.example.PRM.dto.response.OrganizationDetailRes;
import com.example.PRM.entity.OrganizationDetail;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.OrganizationDetailMapper;
import com.example.PRM.repository.OrganizationDetailRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.OrganizationDetailService;
import com.example.PRM.status_enum.VerificationOrganizationStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.example.PRM.exception.IllegalArgumentException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrganizationDetailServiceImpl implements OrganizationDetailService {
    private final OrganizationDetailRepository organizationDetailRepository;
    private final OrganizationDetailMapper organizationDetailMapper;
    private final UserRepository userRepository;

    public OrganizationDetailServiceImpl(OrganizationDetailRepository organizationDetailRepository, OrganizationDetailMapper organizationDetailMapper, UserRepository userRepository) {
        this.organizationDetailRepository = organizationDetailRepository;
        this.organizationDetailMapper = organizationDetailMapper;
        this.userRepository = userRepository;
    }

    @Override
    public void createOrganizationDetail(OrganizationDetailReq organizationDetailReq, UserDetails userDetails) {

        if(organizationDetailReq.getOrgName() == null || organizationDetailReq.getOrgName().isBlank()){
            throw new IllegalArgumentException("Organization name is required");
        }
        if(organizationDetailReq.getAddress() == null || organizationDetailReq.getAddress().isBlank()){
            throw new IllegalArgumentException("Address is required");
        }
        if(organizationDetailReq.getDescription() == null || organizationDetailReq.getDescription().isBlank()){
            throw new IllegalArgumentException("Description is required");
        }
        if(organizationDetailReq.getLatitude() == null){
            throw new IllegalArgumentException("Latitude is required");
        }
        if(organizationDetailReq.getLongitude() == null){
            throw new IllegalArgumentException("Longitude is required");
        }

        OrganizationDetail organizationDetail = organizationDetailMapper.toEntity(organizationDetailReq);
        organizationDetail.setUser(userRepository.findByUserName(userDetails.getUsername()).orElseThrow(()
                -> new NotFoundException("User not found")));
        organizationDetailRepository.save(organizationDetail);
    }

    @Override
    public void updateOrganizationDetail(UUID organizationDetailId, OrganizationDetailReq organizationDetailReq) {
        OrganizationDetail od = organizationDetailRepository.findById(organizationDetailId).orElseThrow(()
                -> new NotFoundException("Organization detail not found with id: " + organizationDetailId));
        if(organizationDetailReq.getOrgName() != null){
            od.setOrgName(organizationDetailReq.getOrgName());
        }
        if(organizationDetailReq.getAddress() != null){
            od.setAddress(organizationDetailReq.getAddress());
        }
        if(organizationDetailReq.getDescription() != null){
            od.setDescription(organizationDetailReq.getDescription());
        }
        if(organizationDetailReq.getLatitude() != null && organizationDetailReq.getLatitude().compareTo(BigDecimal.ZERO) > 0){
            od.setLatitude(organizationDetailReq.getLatitude());
        }
        if(organizationDetailReq.getLongitude() != null && organizationDetailReq.getLongitude().compareTo(BigDecimal.ZERO) > 0){
            od.setLongitude(organizationDetailReq.getLongitude());
        }
        organizationDetailRepository.save(od);

    }

    @Override
    public void deleteOrganizationDetail(UUID organizationId, UserDetails userDetails) {
        OrganizationDetail od = organizationDetailRepository.findById(organizationId).orElseThrow(()
                -> new NotFoundException("Organization detail not found with id: " + organizationId));
        if(!od.getUser().getUserName().equals(userDetails.getUsername())){
            throw new ForbiddenException("You are not authorized to delete this organization detail");
        }
        organizationDetailRepository.delete(od);
    }

    @Override
    public OrganizationDetailRes getOrganizationDetail(UUID organizationId) {
        OrganizationDetail od = organizationDetailRepository.findById(organizationId).orElseThrow(()
                -> new NotFoundException("Organization detail not found with id: " + organizationId));
        return organizationDetailMapper.toResponse(od);
    }

    @Override
    public List<OrganizationDetailRes> getAllOrganizations() {
        return organizationDetailRepository.findAll()
                .stream()
                .map(organizationDetailMapper::toResponse)
                .toList();
    }

    @Override
    public List<OrganizationDetailRes> getPendingOrganizations() {
        return organizationDetailRepository.findByStatus(VerificationOrganizationStatus.PENDING)
                .stream()
                .map(organizationDetailMapper::toResponse)
                .toList();
    }

    @Override
    public void approveOrganization(UUID organizationId) {
        OrganizationDetail od = organizationDetailRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found with id: " + organizationId));
        if(od.getStatus() != VerificationOrganizationStatus.PENDING){
            throw new IllegalArgumentException("Organization is not in pending status");
        }
        od.setStatus(VerificationOrganizationStatus.APPROVED);
        organizationDetailRepository.save(od);
    }

    @Override
    public void rejectOrganization(UUID organizationId) {
        OrganizationDetail od = organizationDetailRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found with id: " + organizationId));
        if(od.getStatus() != VerificationOrganizationStatus.PENDING){
            throw new BadRequestException("Organization is not in pending status");
        }
        od.setStatus(VerificationOrganizationStatus.REJECTED);
        organizationDetailRepository.save(od);
    }

}
