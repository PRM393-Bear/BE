package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.donationRequest.DonationRequestCustomReq;
import com.example.PRM.dto.request.donationRequest.DonationRequestReq;
import com.example.PRM.dto.request.donationRequest.ReceivedReq;
import com.example.PRM.dto.request.donationRequest.ShippingReq;
import com.example.PRM.dto.response.DonationPendingResponse;
import com.example.PRM.dto.response.UploadRes;
import com.example.PRM.dto.response.donationRequest.DonationRequestResponse;
import com.example.PRM.entity.*;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.DonationRequestMapper;
import com.example.PRM.mapper.WardrobeItemMapper;
import com.example.PRM.repository.*;
import com.example.PRM.status_enum.DonationStatus;
import com.example.PRM.status_enum.EventStatus;
import com.example.PRM.status_enum.WardrobeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

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
        donationEvent.setStatus(EventStatus.ONGOING);

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
        donationRequest.setImages(new ArrayList<>());
        donationRequest.setStatus(DonationStatus.PENDING);
        donationRequest.setCreatedAt(LocalDateTime.now());
    }

    // CREATE
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
        item.setStatus(WardrobeStatus.DONATED); 
        
        DonationRequestReq req = new DonationRequestReq();
        req.setDonationEventId(donationEventId);
        req.setWardrobeItemIds(Collections.singletonList(wardrobeItemId));

        when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
        when(wardrobeItemRepository.findAllById(any())).thenReturn(Collections.singletonList(item));

        assertThrows(BadRequestException.class, () -> donationRequestService.createDonationRequest(req, userDetails));
    }

    @Test
    void createDonationRequest_ShouldThrowNotFound_WhenEventNotFound() {
        DonationRequestReq req = new DonationRequestReq();
        req.setDonationEventId(donationEventId);
        when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> donationRequestService.createDonationRequest(req, userDetails));
    }

    @Test
    void createDonationRequest_ShouldThrowBadRequest_WhenEventClosed() {
        donationEvent.setStatus(EventStatus.COMPLETED);
        DonationRequestReq req = new DonationRequestReq();
        req.setDonationEventId(donationEventId);
        when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));

        assertThrows(BadRequestException.class, () -> donationRequestService.createDonationRequest(req, userDetails));

        donationEvent.setStatus(EventStatus.CANCELLED);
        assertThrows(BadRequestException.class, () -> donationRequestService.createDonationRequest(req, userDetails));
    }

    @Test
    void createDonationRequest_ShouldThrowBadRequest_WhenIdsNullOrEmpty() {
        DonationRequestReq req = new DonationRequestReq();
        req.setDonationEventId(donationEventId);
        req.setWardrobeItemIds(Collections.emptyList());
        when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));

        assertThrows(BadRequestException.class, () -> donationRequestService.createDonationRequest(req, userDetails));
        
        req.setWardrobeItemIds(null);
        assertThrows(BadRequestException.class, () -> donationRequestService.createDonationRequest(req, userDetails));
    }

    @Test
    void createDonationRequest_ShouldThrowNotFound_WhenMissingItems() {
        DonationRequestReq req = new DonationRequestReq();
        req.setDonationEventId(donationEventId);
        req.setWardrobeItemIds(Arrays.asList(wardrobeItemId, UUID.randomUUID()));
        when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
        when(wardrobeItemRepository.findAllById(any())).thenReturn(Collections.singletonList(item));

        assertThrows(NotFoundException.class, () -> donationRequestService.createDonationRequest(req, userDetails));
    }

    @Test
    void createDonationRequest_ShouldThrowBadRequest_WhenUserNotOwner() {
        User anotherUser = new User();
        anotherUser.setUserName("anotherUser");
        item.setUser(anotherUser);
        DonationRequestReq req = new DonationRequestReq();
        req.setDonationEventId(donationEventId);
        req.setWardrobeItemIds(Collections.singletonList(wardrobeItemId));
        when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
        when(wardrobeItemRepository.findAllById(any())).thenReturn(Collections.singletonList(item));
        when(userDetails.getUsername()).thenReturn("donorUser");

        assertThrows(BadRequestException.class, () -> donationRequestService.createDonationRequest(req, userDetails));
    }

    // CREATE CUSTOM
    @Test
    void createCustom_ShouldCreate_WhenValid() {
        DonationRequestCustomReq req = new DonationRequestCustomReq();
        req.setDonationEventId(donationEventId);
        MultipartFile mockImage = mock(MultipartFile.class);
        req.setImage(mockImage);

        UploadRes uploadRes = new UploadRes("http://image.url", "publicId");

        when(userDetails.getUsername()).thenReturn("donorUser");
        when(userRepository.findByUserName("donorUser")).thenReturn(Optional.of(user));
        when(uploadService.uploadImage(mockImage, "donorUser")).thenReturn(uploadRes);
        when(wardrobeItemMapper.toEntity(req)).thenReturn(new WardrobeItem());
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
        
        DonationRequest dr = new DonationRequest();
        dr.setImages(new ArrayList<>());
        dr.setItems(new ArrayList<>());
        when(donationRequestMapper.toEntity(req)).thenReturn(dr);

        DonationRequest result = donationRequestService.createDonationRequest(req, userDetails);

        assertNotNull(result);
        assertEquals(DonationStatus.PENDING, result.getStatus());
        verify(donationRequestRepository, times(1)).save(any(DonationRequest.class));
        verify(wardrobeItemRepository, times(1)).save(any(WardrobeItem.class));
    }

    @Test
    void createCustom_ShouldThrowNotFound_WhenUserNotFound() {
        DonationRequestCustomReq req = new DonationRequestCustomReq();
        when(userDetails.getUsername()).thenReturn("unknown");
        when(userRepository.findByUserName("unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> donationRequestService.createDonationRequest(req, userDetails));
    }

    @Test
    void createCustom_ShouldThrowNotFound_WhenEventNotFound() {
        DonationRequestCustomReq req = new DonationRequestCustomReq();
        req.setDonationEventId(donationEventId);
        MultipartFile mockImage = mock(MultipartFile.class);
        req.setImage(mockImage);

        when(userDetails.getUsername()).thenReturn("donorUser");
        when(userRepository.findByUserName("donorUser")).thenReturn(Optional.of(user));
        when(uploadService.uploadImage(any(), any())).thenReturn(new UploadRes("url", "publicId"));
        when(wardrobeItemMapper.toEntity(req)).thenReturn(new WardrobeItem());
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> donationRequestService.createDonationRequest(req, userDetails));
    }

    @Test
    void createCustom_ShouldThrowBadRequest_WhenEventClosed() {
        DonationRequestCustomReq req = new DonationRequestCustomReq();
        req.setDonationEventId(donationEventId);
        MultipartFile mockImage = mock(MultipartFile.class);
        req.setImage(mockImage);
        donationEvent.setStatus(EventStatus.CANCELLED);

        when(userDetails.getUsername()).thenReturn("donorUser");
        when(userRepository.findByUserName("donorUser")).thenReturn(Optional.of(user));
        when(uploadService.uploadImage(any(), any())).thenReturn(new UploadRes("url", "publicId"));
        when(wardrobeItemMapper.toEntity(req)).thenReturn(new WardrobeItem());
        when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));

        assertThrows(BadRequestException.class, () -> donationRequestService.createDonationRequest(req, userDetails));

        donationEvent.setStatus(EventStatus.COMPLETED);
        assertThrows(BadRequestException.class, () -> donationRequestService.createDonationRequest(req, userDetails));
    }

    // ACCEPT
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
    void accept_ShouldThrowNotFound_WhenRequestNotFound() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationRequestService.accept(donationRequestId, userDetails));
    }

    @Test
    void accept_ShouldThrowBadRequest_WhenNotPending() {
        donationRequest.setStatus(DonationStatus.ACCEPTED);
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("orgUser");

        assertThrows(BadRequestException.class, () -> donationRequestService.accept(donationRequestId, userDetails));
    }

    // REJECT
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
    void reject_ShouldThrowNotFound_WhenRequestNotFound() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationRequestService.reject(donationRequestId, "No", userDetails));
    }

    @Test
    void reject_ShouldThrowBadRequest_WhenUserNotOrg() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("wrongUser");
        assertThrows(BadRequestException.class, () -> donationRequestService.reject(donationRequestId, "No", userDetails));
    }

    @Test
    void reject_ShouldThrowBadRequest_WhenNotPending() {
        donationRequest.setStatus(DonationStatus.SHIPPED);
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("orgUser");
        assertThrows(BadRequestException.class, () -> donationRequestService.reject(donationRequestId, "No", userDetails));
    }

    // SHIPPING
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
    void shipping_ShouldThrowNotFound_WhenNotFound() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationRequestService.shipping(donationRequestId, userDetails));
    }

    @Test
    void shipping_ShouldThrowBadRequest_WhenUserNotOwner() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("otherUser");
        assertThrows(BadRequestException.class, () -> donationRequestService.shipping(donationRequestId, userDetails));
    }

    @Test
    void shipping_ShouldThrowBadRequest_WhenNotAccepted() {
        donationRequest.setStatus(DonationStatus.PENDING);
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("donorUser");
        assertThrows(BadRequestException.class, () -> donationRequestService.shipping(donationRequestId, userDetails));
    }

    // SHIPPED
    @Test
    void shipped_ShouldUpdateStatus_WhenValid() {
        donationRequest.setStatus(DonationStatus.SHIPPING);
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("donorUser");

        ShippingReq req = new ShippingReq();
        req.setTrackingCode("TR123");
        MultipartFile mockFile = mock(MultipartFile.class);
        req.setShippingProofFile(mockFile);
        UploadRes upRes = new UploadRes("url", "publicId");
        when(uploadService.uploadImage(mockFile, "donorUser")).thenReturn(upRes);

        DonationRequest result = donationRequestService.shipped(donationRequestId, req, userDetails);

        assertEquals(DonationStatus.SHIPPED, result.getStatus());
        assertEquals("TR123", result.getTrackingCode());
        assertEquals("url", result.getShippingProofUrl());
        verify(donationRequestRepository, times(1)).save(donationRequest);
    }

    @Test
    void shipped_ShouldThrowNotFound_WhenNotFound() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationRequestService.shipped(donationRequestId, new ShippingReq(), userDetails));
    }

    @Test
    void shipped_ShouldThrowBadRequest_WhenUserNotOwner() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("otherUser");
        assertThrows(BadRequestException.class, () -> donationRequestService.shipped(donationRequestId, new ShippingReq(), userDetails));
    }

    @Test
    void shipped_ShouldThrowBadRequest_WhenNotShipping() {
        donationRequest.setStatus(DonationStatus.ACCEPTED);
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("donorUser");
        assertThrows(BadRequestException.class, () -> donationRequestService.shipped(donationRequestId, new ShippingReq(), userDetails));
    }

    // RECEIVED
    @Test
    void received_ShouldUpdateStatus_WhenValid() {
        donationRequest.setStatus(DonationStatus.SHIPPED);
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("orgUser");

        ReceivedReq req = new ReceivedReq();
        MultipartFile mockFile = mock(MultipartFile.class);
        req.setReceiptProofFile(mockFile);
        UploadRes upRes = new UploadRes("url", "publicId");
        when(uploadService.uploadImage(mockFile, "orgUser")).thenReturn(upRes);

        DonationRequest result = donationRequestService.received(donationRequestId, req, userDetails);

        assertEquals(DonationStatus.RECEIVED, result.getStatus());
        assertEquals("url", result.getReceiptProofUrl());
        assertEquals(WardrobeStatus.DONATED, item.getStatus());
        verify(donationRequestRepository, times(1)).save(donationRequest);
        verify(donationEventRepository, times(1)).save(donationEvent);
        verify(wardrobeItemRepository, times(1)).save(any(WardrobeItem.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void received_ShouldUpdateStatus_WhenStatusIsShipping() {
        donationRequest.setStatus(DonationStatus.SHIPPING);
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("orgUser");

        ReceivedReq req = new ReceivedReq();
        MultipartFile mockFile = mock(MultipartFile.class);
        req.setReceiptProofFile(mockFile);
        UploadRes upRes = new UploadRes("url", "publicId");
        when(uploadService.uploadImage(mockFile, "orgUser")).thenReturn(upRes);

        DonationRequest result = donationRequestService.received(donationRequestId, req, userDetails);

        assertEquals(DonationStatus.RECEIVED, result.getStatus());
    }

    @Test
    void received_ShouldThrowNotFound_WhenNotFound() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationRequestService.received(donationRequestId, new ReceivedReq(), userDetails));
    }

    @Test
    void received_ShouldThrowBadRequest_WhenUserNotOrg() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("wrongUser");
        assertThrows(BadRequestException.class, () -> donationRequestService.received(donationRequestId, new ReceivedReq(), userDetails));
    }

    @Test
    void received_ShouldThrowBadRequest_WhenNotShippedOrShipping() {
        donationRequest.setStatus(DonationStatus.PENDING);
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("orgUser");
        assertThrows(BadRequestException.class, () -> donationRequestService.received(donationRequestId, new ReceivedReq(), userDetails));
    }

    // COMPLETED
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

    @Test
    void completed_ShouldThrowNotFound_WhenNotFound() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationRequestService.completed(donationRequestId));
    }

    @Test
    void completed_ShouldThrowBadRequest_WhenShippedButNot10Days() {
        donationRequest.setStatus(DonationStatus.SHIPPED);
        donationRequest.setShippedAt(LocalDateTime.now().minusDays(5));
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));

        assertThrows(BadRequestException.class, () -> donationRequestService.completed(donationRequestId));
        
        donationRequest.setShippedAt(null);
        assertThrows(BadRequestException.class, () -> donationRequestService.completed(donationRequestId));
    }

    @Test
    void completed_ShouldComplete_WhenShippedAnd10DaysPassed() {
        donationRequest.setStatus(DonationStatus.SHIPPED);
        donationRequest.setShippedAt(LocalDateTime.now().minusDays(11));
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));

        DonationRequest result = donationRequestService.completed(donationRequestId);

        assertEquals(DonationStatus.COMPLETED, result.getStatus());
    }

    @Test
    void completed_ShouldThrowBadRequest_WhenOtherStatus() {
        donationRequest.setStatus(DonationStatus.PENDING);
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        assertThrows(BadRequestException.class, () -> donationRequestService.completed(donationRequestId));
    }

    // CANCEL
    @Test
    void cancel_ShouldCancel_WhenValid() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("donorUser");

        DonationRequest result = donationRequestService.cancel(donationRequestId, "Cancel reason", userDetails);

        assertEquals(DonationStatus.CANCELLED, result.getStatus());
        assertEquals("Cancel reason", result.getCancelReason());
        verify(donationRequestRepository, times(1)).save(donationRequest);
    }

    @Test
    void cancel_ShouldThrowNotFound_WhenNotFound() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationRequestService.cancel(donationRequestId, "Reason", userDetails));
    }

    @Test
    void cancel_ShouldThrowBadRequest_WhenUserNotOwner() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("otherUser");
        assertThrows(BadRequestException.class, () -> donationRequestService.cancel(donationRequestId, "Reason", userDetails));
    }

    @Test
    void cancel_ShouldThrowBadRequest_WhenNotPendingOrAccepted() {
        donationRequest.setStatus(DonationStatus.SHIPPED);
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        when(userDetails.getUsername()).thenReturn("donorUser");
        assertThrows(BadRequestException.class, () -> donationRequestService.cancel(donationRequestId, "Reason", userDetails));
    }

    // SCHEDULE / ASSIGN
    @Test
    void checkPendingDonations_ShouldNotify() {
        when(donationRequestRepository.findPendingDonationsOverdue(eq(DonationStatus.PENDING), any()))
                .thenReturn(Collections.singletonList(donationRequest));

        donationRequestService.checkPendingDonations();

        verify(notificationAdminService, times(1)).notifyAdminPendingOverdue(anyList());
    }

    @Test
    void assignOrganization_ShouldAssign_WhenValid() {
        donationRequest.setCreatedAt(LocalDateTime.now().minusDays(6));
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        
        OrganizationDetail newOrg = new OrganizationDetail();
        newOrg.setId(UUID.randomUUID());
        when(organizationDetailRepository.findById(newOrg.getId())).thenReturn(Optional.of(newOrg));

        DonationRequest result = donationRequestService.assignOrganization(donationRequestId, newOrg.getId());

        assertEquals(newOrg, result.getOrganizationDetail());
        verify(donationRequestRepository, times(1)).save(donationRequest);
    }

    @Test
    void assignOrganization_ShouldThrowNotFound_WhenNotFound() {
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationRequestService.assignOrganization(donationRequestId, UUID.randomUUID()));
    }

    @Test
    void assignOrganization_ShouldThrowBadRequest_WhenNotOldEnough() {
        donationRequest.setCreatedAt(LocalDateTime.now().minusDays(2));
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        assertThrows(BadRequestException.class, () -> donationRequestService.assignOrganization(donationRequestId, UUID.randomUUID()));
    }

    @Test
    void assignOrganization_ShouldThrowNotFound_WhenOrgNotFound() {
        donationRequest.setCreatedAt(LocalDateTime.now().minusDays(6));
        when(donationRequestRepository.findById(donationRequestId)).thenReturn(Optional.of(donationRequest));
        UUID newOrgId = UUID.randomUUID();
        when(organizationDetailRepository.findById(newOrgId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationRequestService.assignOrganization(donationRequestId, newOrgId));
    }

    // GET
    @Test
    void getPendingDonations_ShouldReturn_WhenFound() {
        when(donationRequestRepository.findPendingDonationsOverdue(eq(DonationStatus.PENDING), any()))
                .thenReturn(Collections.singletonList(donationRequest));
        when(donationRequestMapper.toPendingResponse(any())).thenReturn(new DonationPendingResponse());

        List<DonationPendingResponse> res = donationRequestService.getPendingDonations(userDetails);

        assertEquals(1, res.size());
    }

    @Test
    void getPendingDonations_ShouldThrowNotFound_WhenEmpty() {
        when(donationRequestRepository.findPendingDonationsOverdue(eq(DonationStatus.PENDING), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class, () -> donationRequestService.getPendingDonations(userDetails));
    }

    @Test
    void getAllDonationRequestsFromOrganizationId_ShouldReturn_WhenFound() {
        when(donationRequestRepository.findByOrganizationDetail_Id(orgDetail.getId()))
                .thenReturn(Collections.singletonList(donationRequest));
        when(donationRequestMapper.toResponse(any())).thenReturn(new DonationRequestResponse());

        List<DonationRequestResponse> res = donationRequestService.getAllDonationRequestsFromOrganizationId(orgDetail.getId());

        assertEquals(1, res.size());
    }

    @Test
    void getAllDonationRequestsFromOrganizationId_ShouldThrowNotFound_WhenEmpty() {
        when(donationRequestRepository.findByOrganizationDetail_Id(orgDetail.getId()))
                .thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class, () -> donationRequestService.getAllDonationRequestsFromOrganizationId(orgDetail.getId()));
    }

    @Test
    void getAllDonationRequestsFromUser_ShouldReturn_WhenFound() {
        when(userDetails.getUsername()).thenReturn("donorUser");
        when(userRepository.findByUserName("donorUser")).thenReturn(Optional.of(user));
        when(donationRequestRepository.findByUser_UserId(user.getUserId())).thenReturn(Collections.singletonList(donationRequest));
        when(donationRequestMapper.toResponse(any())).thenReturn(new DonationRequestResponse());

        List<DonationRequestResponse> res = donationRequestService.getAllDonationRequestsFromUser(userDetails);

        assertEquals(1, res.size());
    }

    @Test
    void getAllDonationRequestsFromUser_ShouldThrowNotFound_WhenUserNotFound() {
        when(userDetails.getUsername()).thenReturn("donorUser");
        when(userRepository.findByUserName("donorUser")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> donationRequestService.getAllDonationRequestsFromUser(userDetails));
    }

    @Test
    void autoCheckReceivedDonations_ShouldNotify_WhenNotRemindedRecently() {
        donationRequest.setReminderSentAt(null);
        when(donationRequestRepository.findByStatusAndUpdatedAtBefore(eq(DonationStatus.SHIPPING), any()))
                .thenReturn(Collections.singletonList(donationRequest));

        donationRequestService.autoCheckReceivedDonations();

        assertNotNull(donationRequest.getReminderSentAt());
        verify(donationRequestRepository, times(1)).save(donationRequest);
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void autoCheckReceivedDonations_ShouldNotify_WhenRemindedLongAgo() {
        donationRequest.setReminderSentAt(LocalDateTime.now().minusDays(5));
        when(donationRequestRepository.findByStatusAndUpdatedAtBefore(eq(DonationStatus.SHIPPING), any()))
                .thenReturn(Collections.singletonList(donationRequest));

        donationRequestService.autoCheckReceivedDonations();

        verify(donationRequestRepository, times(1)).save(donationRequest);
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void autoCheckReceivedDonations_ShouldNotNotify_WhenRemindedRecently() {
        donationRequest.setReminderSentAt(LocalDateTime.now().minusDays(1));
        when(donationRequestRepository.findByStatusAndUpdatedAtBefore(eq(DonationStatus.SHIPPING), any()))
                .thenReturn(Collections.singletonList(donationRequest));

        donationRequestService.autoCheckReceivedDonations();

        verify(donationRequestRepository, never()).save(donationRequest);
        verify(eventPublisher, never()).publishEvent(any());
    }
}
