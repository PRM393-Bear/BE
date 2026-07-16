package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.donationRequest.DonationRequestReq;
import com.example.PRM.entity.DonationEvent;
import com.example.PRM.entity.DonationRequest;
import com.example.PRM.entity.OrganizationDetail;
import com.example.PRM.entity.User;
import com.example.PRM.entity.WardrobeItem;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.DonationRequestMapper;
import com.example.PRM.mapper.WardrobeItemMapper;
import com.example.PRM.repository.DonationEventRepository;
import com.example.PRM.repository.DonationRequestRepository;
import com.example.PRM.repository.OrganizationDetailRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.repository.WardrobeItemRepository;
import com.example.PRM.status_enum.DonationStatus;
import com.example.PRM.status_enum.WardrobeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationRequestServiceImplTest {

    @InjectMocks
    private DonationRequestServiceImpl donationRequestService;

    @Mock private DonationRequestRepository donationRequestRepository;
    @Mock private DonationEventRepository donationEventRepository;
    @Mock private WardrobeItemRepository wardrobeItemRepository;
    @Mock private OrganizationDetailRepository organizationDetailRepository;
    @Mock private DonationRequestMapper donationRequestMapper;
    @Mock private UserRepository userRepository;
    @Mock private NotificationAdminServiceImpl notificationAdminService;
    @Mock private UploadServiceImpl uploadService;
    @Mock private WardrobeItemMapper wardrobeItemMapper;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private UserDetails userDetails;

    private User user;
    private User orgUser;
    private OrganizationDetail orgDetail;
    private DonationEvent donationEvent;
    private WardrobeItem item;
    private UUID donationEventId;
    private UUID wardrobeItemId;
    private UUID donationRequestId;
    private DonationRequest donationRequest;

    @BeforeEach
    void setUp() {
        donationEventId = UUID.randomUUID();
        wardrobeItemId = UUID.randomUUID();
        donationRequestId = UUID.randomUUID();

        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("donorUser");

        orgUser = new User();
        orgUser.setUserId(UUID.randomUUID());
        orgUser.setUserName("orgUser");

        orgDetail = new OrganizationDetail();
        orgDetail.setId(UUID.randomUUID());
        orgDetail.setUser(orgUser);

        donationEvent = new DonationEvent();
        donationEvent.setId(donationEventId);
        donationEvent.setOrganizationDetail(orgDetail);
        donationEvent.setCurrentQuantity(0);

        item = new WardrobeItem();
        item.setId(wardrobeItemId);
        item.setStatus(WardrobeStatus.OWNED);
        item.setUser(user);

        donationRequest = new DonationRequest();
        donationRequest.setId(donationRequestId);
        donationRequest.setUser(user);
        donationRequest.setOrganizationDetail(orgDetail);
        donationRequest.setDonationEvent(donationEvent);
        donationRequest.setItems(new ArrayList<>(Collections.singletonList(item)));
        donationRequest.setStatus(DonationStatus.PENDING);
    }

    @Test
    void createDonationRequest_ShouldCreate_WhenValid() {
        DonationRequestReq req = new DonationRequestReq();
        req.setDonationEventId(donationEventId);
        req.setWardrobeItemIds(Collections.singletonList(wardrobeItemId));

        when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
        when(wardrobeItemRepository.findAllById(any())).thenReturn(Collections.singletonList(item));
        when(userDetails.getUsername()).thenReturn("donorUser");

        DonationRequest result = donationRequestService.createDonationRequest(req, userDetails);

        assertNotNull(result);
        assertEquals(DonationStatus.PENDING, result.getStatus());
        assertEquals(WardrobeStatus.LISTED, item.getStatus());
        verify(donationRequestRepository, times(1)).save(any(DonationRequest.class));
    }

    @Test
    void createDonationRequest_ShouldThrowBadRequest_WhenItemNotOwned() {
        item.setStatus(WardrobeStatus.DONATED); // Not OWNED
        
        DonationRequestReq req = new DonationRequestReq();
        req.setDonationEventId(donationEventId);
        req.setWardrobeItemIds(Collections.singletonList(wardrobeItemId));

        when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
        when(wardrobeItemRepository.findAllById(any())).thenReturn(Collections.singletonList(item));

        assertThrows(BadRequestException.class, () -> donationRequestService.createDonationRequest(req, userDetails));
    }

    @Test
    void accept_ShouldAccept_WhenOrganizationMatches() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("orgUser");

        DonationRequest result = donationRequestService.accept(donationRequestId, userDetails);

        assertNotNull(result);
        assertEquals(DonationStatus.ACCEPTED, result.getStatus());
        verify(donationRequestRepository, times(1)).save(donationRequest);
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void accept_ShouldThrowBadRequest_WhenUserIsNotOrganization() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("wrongOrgUser");

        assertThrows(BadRequestException.class, () -> donationRequestService.accept(donationRequestId, userDetails));
    }

    @Test
    void reject_ShouldReject_WhenOrganizationMatches() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("orgUser");

        DonationRequest result = donationRequestService.reject(donationRequestId, "Not needed", userDetails);

        assertNotNull(result);
        assertEquals(DonationStatus.REJECTED, result.getStatus());
        assertEquals("Not needed", result.getRejectedReason());
        verify(donationRequestRepository, times(1)).save(donationRequest);
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void shipping_ShouldUpdateStatusToShipping_WhenUserIsDonor() {
        donationRequest.setStatus(DonationStatus.ACCEPTED);
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("donorUser");

        DonationRequest result = donationRequestService.shipping(donationRequestId, userDetails);

        assertNotNull(result);
        assertEquals(DonationStatus.SHIPPING, result.getStatus());
        verify(donationRequestRepository, times(1)).save(donationRequest);
    }

    @Test
    void completed_ShouldComplete_WhenStatusIsReceived() {
        donationRequest.setStatus(DonationStatus.RECEIVED);
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));

        DonationRequest result = donationRequestService.completed(donationRequestId);

        assertNotNull(result);
        assertEquals(DonationStatus.COMPLETED, result.getStatus());
        assertEquals(WardrobeStatus.DONATED, item.getStatus());
        verify(donationEventRepository, times(1)).save(donationEvent);
        verify(donationRequestRepository, times(1)).save(donationRequest);
        verify(wardrobeItemRepository, times(1)).save(item);
    }
}
