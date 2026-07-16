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
    private OrganizationDetail organizationDetail;
    private UUID orgId;
    private OrganizationDetailReq req;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();

        staffRole = new Role();
        staffRole.setRoleName("STAFF");

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
    void createOrganizationDetail_ShouldThrowIllegalArgument_WhenMissingFields() {
        req.setOrgName(null);
        
        assertThrows(IllegalArgumentException.class, () -> 
            organizationDetailService.createOrganizationDetail(req, userDetails));
        verify(organizationDetailRepository, never()).save(any(OrganizationDetail.class));
    }

    @Test
    void updateOrganizationDetail_ShouldUpdate_WhenUserIsOwner() {
        when(userDetails.getUsername()).thenReturn("orgUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        OrganizationDetailRes result = organizationDetailService.updateOrganizationDetail(orgId, req, userDetails);

        assertNotNull(result);
        verify(organizationDetailRepository, times(1)).save(organizationDetail);
    }

    @Test
    void updateOrganizationDetail_ShouldThrowForbidden_WhenUserIsNotOwner() {
        when(userDetails.getUsername()).thenReturn("otherUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));

        assertThrows(ForbiddenException.class, () -> 
            organizationDetailService.updateOrganizationDetail(orgId, req, userDetails));
    }

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
    void approveOrganization_ShouldThrowIllegalArgument_WhenStatusIsNotPending() {
        organizationDetail.setStatus(VerificationOrganizationStatus.APPROVED);
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));

        assertThrows(IllegalArgumentException.class, () -> 
            organizationDetailService.approveOrganization(orgId, userDetails));
    }

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
    void deleteOrganizationDetail_ShouldDelete_WhenUserIsOwner() {
        when(userDetails.getUsername()).thenReturn("orgUser");
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        when(organizationDetailMapper.toResponse(any())).thenReturn(new OrganizationDetailRes());

        OrganizationDetailRes result = organizationDetailService.deleteOrganizationDetail(orgId, userDetails);

        assertNotNull(result);
        verify(organizationDetailRepository, times(1)).delete(organizationDetail);
    }
}
