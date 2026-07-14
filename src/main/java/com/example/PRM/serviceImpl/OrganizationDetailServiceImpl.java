package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.organizationDetail.OrganizationDetailReq;
import com.example.PRM.dto.response.OrganizationDetailRes;
import com.example.PRM.entity.OrganizationDetail;
import com.example.PRM.entity.User;
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
import java.time.LocalDateTime;
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
        if(organizationDetailReq.getVerificationDocs() != null && !organizationDetailReq.getVerificationDocs().isEmpty()){
            organizationDetail.setStatus(VerificationOrganizationStatus.PENDING);
        }
        organizationDetail.setUser(userRepository
                .findByUserName(userDetails.getUsername())
                .orElseThrow(()
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
    public void approveOrganization(UUID organizationId, UserDetails userDetails) {
        OrganizationDetail od = organizationDetailRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found with id: " + organizationId));
        if(od.getStatus() != VerificationOrganizationStatus.PENDING){
            throw new IllegalArgumentException("Organization is not in pending status");
        }
        User user = userRepository.findByUserName(userDetails.getUsername()).orElseThrow(()
                -> new NotFoundException("User not found with userName: " + userDetails.getUsername()));
        if(!user.getRole().getRoleName().equals("STAFF")){
            throw new IllegalArgumentException("Only staff can approve organization");
        }
        od.setStatus(VerificationOrganizationStatus.APPROVED);
        od.setApprovedAt(LocalDateTime.now());
        od.setApprovedBy(user.getUserId().toString());
        organizationDetailRepository.save(od);
    }

    @Override
    public void rejectOrganization(UUID organizationId, UserDetails userDetails, String reason) {
        OrganizationDetail od = organizationDetailRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found with id: " + organizationId));
        if(od.getStatus() != VerificationOrganizationStatus.PENDING){
            throw new BadRequestException("Organization is not in pending status");
        }
        User user = userRepository.findByUserName(userDetails.getUsername()).orElseThrow(()
                -> new NotFoundException("User not found with userName: " + userDetails.getUsername()));
        if(!user.getRole().getRoleName().equals("STAFF")){
            throw new IllegalArgumentException("Only staff can reject organization");
        }
        od.setStatus(VerificationOrganizationStatus.REJECTED);
        od.setRejectedAt(LocalDateTime.now());
        od.setRejectedBy(user.getUserId().toString());
        od.setRejectedReason(reason);
        organizationDetailRepository.save(od);
    }

    @Override
    public List<OrganizationDetailRes> getNearbyOrganizations(BigDecimal latitude, BigDecimal longitude, double radius) {
        if (latitude == null || longitude == null) {
            throw new BadRequestException("Vĩ độ (latitude) và Kinh độ (longitude) không được để trống");
        }

        double searchRadius = radius > 0 ? radius : 50.0;

        List<OrganizationDetail> nearbyOrgs = organizationDetailRepository.findNearbyOrganizations(latitude, longitude, searchRadius);

        return nearbyOrgs.stream()
                .map(organizationDetailMapper::toResponse)
                .toList();
    }

}
