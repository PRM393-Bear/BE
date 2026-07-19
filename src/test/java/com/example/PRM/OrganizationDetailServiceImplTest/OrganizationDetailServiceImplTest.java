package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.organizationDetail.OrganizationDetailReq;
import com.example.PRM.dto.response.org.OrganizationDetailRes;
import com.example.PRM.entity.OrganizationDetail;
import com.example.PRM.entity.Role;
import com.example.PRM.entity.User;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.OrganizationDetailMapper;
import com.example.PRM.repository.OrganizationDetailRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.status_enum.VerificationOrganizationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.PRM.exception.IllegalArgumentException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationDetailServiceImplTest {

    @InjectMocks
    private OrganizationDetailServiceImpl organizationDetailService;

    @Mock private OrganizationDetailRepository organizationDetailRepository;
    @Mock private OrganizationDetailMapper organizationDetailMapper;
    @Mock private UserRepository userRepository;
    @Mock private EmailServiceImpl emailService;
    @Mock private UserDetails userDetails;

    private User user;
    private User staffUser;
    private Role staffRole;
    private Role userRole;
    private OrganizationDetail organizationDetail;
    private UUID orgId;
    private OrganizationDetailReq req;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();

        staffRole = new Role();
        staffRole.setRoleName("STAFF");

        userRole = new Role();
        userRole.setRoleName("USER");

        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("orgUser");
        user.setEmail("org@example.com");

        staffUser = new User();
        staffUser.setUserId(UUID.randomUUID());
        staffUser.setUserName("staffUser");
        staffUser.setRole(staffRole);

        organizationDetail = new OrganizationDetail();
        organizationDetail.setId(orgId);
        organizationDetail.setUser(user);
        organizationDetail.setStatus(VerificationOrganizationStatus.PENDING);

        req = new OrganizationDetailReq();
        req.setOrgName("Test Org");
        req.setAddress("Test Address");
        req.setDescription("Test Desc");
        req.setLatitude(BigDecimal.valueOf(10.0));
        req.setLongitude(BigDecimal.valueOf(20.0));
    }

    // CREATE
    @Test
    void createOrganizationDetail_ShouldCreate_WhenValidRequest() {
        when(userDetails.getUsername()).thenReturn("orgUser");
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(organizationDetailMapper.toEntity(req)).thenReturn(new OrganizationDetail());
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        OrganizationDetailRes result = organizationDetailService.createOrganizationDetail(req, userDetails);

        assertNotNull(result);
        verify(organizationDetailRepository, times(1)).save(any(OrganizationDetail.class));
    }

    @Test
    void createOrganizationDetail_ShouldSetPending_WhenHasDocs() {
        req.setVerificationDocs(Arrays.asList("doc1.pdf"));
        when(userDetails.getUsername()).thenReturn("orgUser");
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        
        OrganizationDetail mappedEntity = new OrganizationDetail();
        when(organizationDetailMapper.toEntity(req)).thenReturn(mappedEntity);
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        organizationDetailService.createOrganizationDetail(req, userDetails);

        assertEquals(VerificationOrganizationStatus.PENDING, mappedEntity.getStatus());
        verify(organizationDetailRepository, times(1)).save(mappedEntity);
    }

    @Test
    void createOrganizationDetail_ShouldThrowIllegalArgument_WhenOrgNameNull() {
        req.setOrgName(null);
        assertThrows(IllegalArgumentException.class, () -> organizationDetailService.createOrganizationDetail(req, userDetails));
    }

    @Test
    void createOrganizationDetail_ShouldThrowIllegalArgument_WhenAddressNull() {
        req.setAddress(null);
        assertThrows(IllegalArgumentException.class, () -> organizationDetailService.createOrganizationDetail(req, userDetails));
    }

    @Test
    void createOrganizationDetail_ShouldThrowIllegalArgument_WhenDescriptionNull() {
        req.setDescription(null);
        assertThrows(IllegalArgumentException.class, () -> organizationDetailService.createOrganizationDetail(req, userDetails));
    }

    @Test
    void createOrganizationDetail_ShouldThrowIllegalArgument_WhenLatitudeNull() {
        req.setLatitude(null);
        assertThrows(IllegalArgumentException.class, () -> organizationDetailService.createOrganizationDetail(req, userDetails));
    }
    @Test
    void createOrganizationDetail_ShouldThrowIllegalArgument_WhenOrgNameBlank() {
        req.setOrgName("   ");
        assertThrows(IllegalArgumentException.class, () -> organizationDetailService.createOrganizationDetail(req, userDetails));
    }

    @Test
    void createOrganizationDetail_ShouldThrowIllegalArgument_WhenAddressBlank() {
        req.setAddress("   ");
        assertThrows(IllegalArgumentException.class, () -> organizationDetailService.createOrganizationDetail(req, userDetails));
    }

    @Test
    void createOrganizationDetail_ShouldThrowIllegalArgument_WhenDescriptionBlank() {
        req.setDescription("   ");
        assertThrows(IllegalArgumentException.class, () -> organizationDetailService.createOrganizationDetail(req, userDetails));
    }

    @Test
    void updateOrganizationDetail_ShouldIgnoreNullFields_WhenUserIsOwner() {
        OrganizationDetailReq nullReq = new OrganizationDetailReq();
        nullReq.setAcceptedTypes(Collections.emptyList());
        nullReq.setVerificationDocs(Collections.emptyList());

        when(userDetails.getUsername()).thenReturn("orgUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        OrganizationDetailRes result = organizationDetailService.updateOrganizationDetail(orgId, nullReq, userDetails);

        assertNotNull(result);
        verify(organizationDetailRepository, times(1)).save(organizationDetail);
    }

    @Test
    void getNearbyOrganizations_ShouldThrowBadRequest_WhenNullLng() {
        assertThrows(BadRequestException.class, () -> organizationDetailService.getNearbyOrganizations(BigDecimal.valueOf(10.0), null, 10.0));
    }
    @Test
    void createOrganizationDetail_ShouldThrowIllegalArgument_WhenLongitudeNull() {
        req.setLongitude(null);
        assertThrows(IllegalArgumentException.class, () -> organizationDetailService.createOrganizationDetail(req, userDetails));
    }

    @Test
    void createOrganizationDetail_ShouldThrowNotFound_WhenUserNotFound() {
        when(userDetails.getUsername()).thenReturn("orgUser");
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.empty());
        when(organizationDetailMapper.toEntity(req)).thenReturn(new OrganizationDetail());

        assertThrows(NotFoundException.class, () -> organizationDetailService.createOrganizationDetail(req, userDetails));
    }

    // UPDATE
    @Test
    void updateOrganizationDetail_ShouldUpdate_WhenUserIsOwner() {
        req.setWebsiteUrl("http://test.com");
        req.setAvtOrg("avt.jpg");
        req.setAcceptedTypes(Arrays.asList("BOOKS"));
        req.setVerificationDocs(Arrays.asList("doc2.pdf"));

        when(userDetails.getUsername()).thenReturn("orgUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        OrganizationDetailRes result = organizationDetailService.updateOrganizationDetail(orgId, req, userDetails);

        assertNotNull(result);
        assertEquals("http://test.com", organizationDetail.getWebsiteUrl());
        assertEquals("avt.jpg", organizationDetail.getAvtOrg());
        verify(organizationDetailRepository, times(1)).save(organizationDetail);
    }

    @Test
    void updateOrganizationDetail_ShouldThrowNotFound_WhenOrgDetailNotFound() {
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> organizationDetailService.updateOrganizationDetail(orgId, req, userDetails));
    }

    @Test
    void updateOrganizationDetail_ShouldThrowForbidden_WhenUserIsNotOwner() {
        when(userDetails.getUsername()).thenReturn("otherUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));

        assertThrows(ForbiddenException.class, () -> organizationDetailService.updateOrganizationDetail(orgId, req, userDetails));
    }

    // DELETE
    @Test
    void deleteOrganizationDetail_ShouldDelete_WhenUserIsOwner() {
        when(userDetails.getUsername()).thenReturn("orgUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        OrganizationDetailRes result = organizationDetailService.deleteOrganizationDetail(orgId, userDetails);

        assertNotNull(result);
        verify(organizationDetailRepository, times(1)).delete(organizationDetail);
    }

    @Test
    void deleteOrganizationDetail_ShouldThrowNotFound_WhenOrgDetailNotFound() {
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> organizationDetailService.deleteOrganizationDetail(orgId, userDetails));
    }

    @Test
    void deleteOrganizationDetail_ShouldThrowForbidden_WhenUserIsNotOwner() {
        when(userDetails.getUsername()).thenReturn("otherUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        assertThrows(ForbiddenException.class, () -> organizationDetailService.deleteOrganizationDetail(orgId, userDetails));
    }

    // GET
    @Test
    void getOrganizationDetail_ShouldReturn() {
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        OrganizationDetailRes res = organizationDetailService.getOrganizationDetail(orgId);
        assertNotNull(res);
    }

    @Test
    void getOrganizationDetail_ShouldThrowNotFound_WhenNotExists() {
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> organizationDetailService.getOrganizationDetail(orgId));
    }

    @Test
    void getAllOrganizations_ShouldReturnList() {
        when(organizationDetailRepository.findAll()).thenReturn(Collections.singletonList(organizationDetail));
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        List<OrganizationDetailRes> res = organizationDetailService.getAllOrganizations();
        assertEquals(1, res.size());
    }

    @Test
    void getPendingOrganizations_ShouldReturnList() {
        when(organizationDetailRepository.findByStatus(VerificationOrganizationStatus.PENDING)).thenReturn(Collections.singletonList(organizationDetail));
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        List<OrganizationDetailRes> res = organizationDetailService.getPendingOrganizations();
        assertEquals(1, res.size());
    }

    // APPROVE
    @Test
    void approveOrganization_ShouldApprove_WhenUserIsStaff() {
        when(userDetails.getUsername()).thenReturn("staffUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        when(userRepository.findByUserName("staffUser")).thenReturn(Optional.of(staffUser));
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        OrganizationDetailRes result = organizationDetailService.approveOrganization(orgId, userDetails);

        assertNotNull(result);
        assertEquals(VerificationOrganizationStatus.APPROVED, organizationDetail.getStatus());
        verify(organizationDetailRepository, times(1)).save(organizationDetail);
        verify(emailService, times(1)).sendApprovalEmail(user.getEmail());
    }

    @Test
    void approveOrganization_ShouldThrowNotFound_WhenOrgDetailNotFound() {
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> organizationDetailService.approveOrganization(orgId, userDetails));
    }

    @Test
    void approveOrganization_ShouldThrowIllegalArgument_WhenStatusIsNotPending() {
        organizationDetail.setStatus(VerificationOrganizationStatus.APPROVED);
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));

        assertThrows(IllegalArgumentException.class, () -> organizationDetailService.approveOrganization(orgId, userDetails));
    }

    @Test
    void approveOrganization_ShouldThrowNotFound_WhenUserNotFound() {
        when(userDetails.getUsername()).thenReturn("staffUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        when(userRepository.findByUserName("staffUser")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> organizationDetailService.approveOrganization(orgId, userDetails));
    }

    @Test
    void approveOrganization_ShouldThrowIllegalArgument_WhenUserNotStaff() {
        staffUser.setRole(userRole); // Not STAFF
        when(userDetails.getUsername()).thenReturn("staffUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        when(userRepository.findByUserName("staffUser")).thenReturn(Optional.of(staffUser));

        assertThrows(IllegalArgumentException.class, () -> organizationDetailService.approveOrganization(orgId, userDetails));
    }

    // REJECT
    @Test
    void rejectOrganization_ShouldReject_WhenUserIsStaff() {
        when(userDetails.getUsername()).thenReturn("staffUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        when(userRepository.findByUserName("staffUser")).thenReturn(Optional.of(staffUser));
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        OrganizationDetailRes result = organizationDetailService.rejectOrganization(orgId, userDetails, "Missing docs");

        assertNotNull(result);
        assertEquals(VerificationOrganizationStatus.REJECTED, organizationDetail.getStatus());
        assertEquals("Missing docs", organizationDetail.getRejectedReason());
        verify(organizationDetailRepository, times(1)).save(organizationDetail);
        verify(emailService, times(1)).sendRejectEmail(user.getEmail(), "Missing docs");
    }

    @Test
    void rejectOrganization_ShouldThrowNotFound_WhenOrgDetailNotFound() {
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> organizationDetailService.rejectOrganization(orgId, userDetails, "reason"));
    }

    @Test
    void rejectOrganization_ShouldThrowBadRequest_WhenStatusIsNotPending() {
        organizationDetail.setStatus(VerificationOrganizationStatus.APPROVED);
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));

        assertThrows(BadRequestException.class, () -> organizationDetailService.rejectOrganization(orgId, userDetails, "reason"));
    }

    @Test
    void rejectOrganization_ShouldThrowNotFound_WhenUserNotFound() {
        when(userDetails.getUsername()).thenReturn("staffUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        when(userRepository.findByUserName("staffUser")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> organizationDetailService.rejectOrganization(orgId, userDetails, "reason"));
    }

    @Test
    void rejectOrganization_ShouldThrowIllegalArgument_WhenUserNotStaff() {
        staffUser.setRole(userRole); // Not STAFF
        when(userDetails.getUsername()).thenReturn("staffUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        when(userRepository.findByUserName("staffUser")).thenReturn(Optional.of(staffUser));

        assertThrows(IllegalArgumentException.class, () -> organizationDetailService.rejectOrganization(orgId, userDetails, "reason"));
    }

    // GET NEARBY
    @Test
    void getNearbyOrganizations_ShouldReturnList() {
        when(organizationDetailRepository.findNearbyOrganizations(any(), any(), anyDouble()))
                .thenReturn(Collections.singletonList(organizationDetail));
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        List<OrganizationDetailRes> res = organizationDetailService.getNearbyOrganizations(BigDecimal.valueOf(10.0), BigDecimal.valueOf(106.0), 10.0);
        assertEquals(1, res.size());
    }

    @Test
    void getNearbyOrganizations_ShouldUseDefaultRadius() {
        when(organizationDetailRepository.findNearbyOrganizations(any(), any(), eq(50.0)))
                .thenReturn(Collections.singletonList(organizationDetail));
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        List<OrganizationDetailRes> res = organizationDetailService.getNearbyOrganizations(BigDecimal.valueOf(10.0), BigDecimal.valueOf(106.0), 0);
        assertEquals(1, res.size());
    }

    @Test
    void getNearbyOrganizations_ShouldThrowBadRequest_WhenNullLat() {
        assertThrows(BadRequestException.class, () -> organizationDetailService.getNearbyOrganizations(null, BigDecimal.valueOf(106.0), 10.0));
    }

    // GET BY USER ID
    @Test
    void getOrganizationDetailByUserId_ShouldReturn() {
        when(userDetails.getUsername()).thenReturn("orgUser");
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(organizationDetailRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.of(organizationDetail));
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        OrganizationDetailRes res = organizationDetailService.getOrganizationDetailByUserId(userDetails);
        assertNotNull(res);
    }

    @Test
    void getOrganizationDetailByUserId_ShouldThrowNotFound_WhenUserNotFound() {
        when(userDetails.getUsername()).thenReturn("orgUser");
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> organizationDetailService.getOrganizationDetailByUserId(userDetails));
    }

    @Test
    void getOrganizationDetailByUserId_ShouldThrowNotFound_WhenOrgDetailNotFound() {
        when(userDetails.getUsername()).thenReturn("orgUser");
        when(userRepository.findByUserName("orgUser")).thenReturn(Optional.of(user));
        when(organizationDetailRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> organizationDetailService.getOrganizationDetailByUserId(userDetails));
    }
}
