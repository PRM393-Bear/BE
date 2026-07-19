package com.example.PRM.DonationRequestServiceImplTest;

import com.example.PRM.dto.request.donationRequest.DonationRequestCustomReq;
import com.example.PRM.dto.request.donationRequest.DonationRequestReq;
import com.example.PRM.dto.request.donationRequest.ReceivedReq;
import com.example.PRM.dto.request.donationRequest.ShippingReq;
import com.example.PRM.dto.response.DonationPendingResponse;
import com.example.PRM.dto.response.UploadRes;
import com.example.PRM.dto.response.donationRequest.DonationRequestResponse;
import com.example.PRM.entity.*;
import com.example.PRM.event.DonationNotificationEvent;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.DonationRequestMapper;
import com.example.PRM.mapper.WardrobeItemMapper;
import com.example.PRM.repository.*;
import com.example.PRM.serviceImpl.DonationRequestServiceImpl;
import com.example.PRM.serviceImpl.NotificationAdminServiceImpl;
import com.example.PRM.serviceImpl.UploadServiceImpl;
import com.example.PRM.status_enum.DonationStatus;
import com.example.PRM.status_enum.EventStatus;
import com.example.PRM.status_enum.WardrobeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho DonationRequestServiceImpl.
 * <p>
 * GIẢ ĐỊNH: BadRequestException và NotFoundException là RuntimeException với
 * constructor nhận 1 tham số String message (đúng theo cách chúng được dùng
 * xuyên suốt code nguồn). Nếu thực tế khác, cần chỉnh lại các dòng ném lỗi.
 */
@ExtendWith(MockitoExtension.class)
class DonationRequestServiceImplTest {

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

    private DonationRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DonationRequestServiceImpl(
                donationRequestRepository, donationEventRepository, wardrobeItemRepository,
                organizationDetailRepository, donationRequestMapper, userRepository,
                notificationAdminService, uploadService, wardrobeItemMapper, eventPublisher);
    }

    // ==================== helpers ====================

    private User buildUser(String username) {
        User u = new User();
        u.setUserId(UUID.randomUUID());
        u.setUserName(username);
        return u;
    }

    private OrganizationDetail buildOrg(User orgUser) {
        OrganizationDetail od = new OrganizationDetail();
        od.setId(UUID.randomUUID());
        od.setUser(orgUser);
        od.setOrgName("Org " + orgUser.getUserName());
        return od;
    }

    private DonationEvent buildEvent(EventStatus status, OrganizationDetail org) {
        DonationEvent de = new DonationEvent();
        de.setId(UUID.randomUUID());
        de.setStatus(status);
        de.setOrganizationDetail(org);
        de.setCurrentQuantity(0);
        de.setTitle("Event");
        return de;
    }

    private WardrobeItem buildItem(User owner, WardrobeStatus status) {
        WardrobeItem wi = new WardrobeItem();
        wi.setId(UUID.randomUUID());
        wi.setUser(owner);
        wi.setStatus(status);
        return wi;
    }

    private DonationRequest buildDonationRequest(User user, OrganizationDetail org, DonationStatus status) {
        DonationRequest dr = new DonationRequest();
        dr.setId(UUID.randomUUID());
        dr.setUser(user);
        dr.setOrganizationDetail(org);
        dr.setStatus(status);
        return dr;
    }

    // ==================== createDonationRequest(DonationRequestReq) ====================

    @Nested
    class CreateDonationRequestReq {

        @Test
        void success() {
            User user = buildUser("alice");
            OrganizationDetail org = buildOrg(buildUser("orgOwner"));
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);

            UUID itemId = UUID.randomUUID();
            WardrobeItem item = buildItem(user, WardrobeStatus.OWNED);

            DonationRequestReq req = new DonationRequestReq(event.getId(), "desc", List.of(itemId));
            DonationRequest mapped = new DonationRequest();

            when(donationRequestMapper.toEntity(req)).thenReturn(mapped);
            when(donationEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
            when(wardrobeItemRepository.findAllById(List.of(itemId))).thenReturn(List.of(item));
            when(userDetails.getUsername()).thenReturn("alice");

            DonationRequest result = service.createDonationRequest(req, userDetails);

            assertEquals(DonationStatus.PENDING, result.getStatus());
            assertEquals(user, result.getUser());
            assertEquals(org, result.getOrganizationDetail());
            assertEquals(WardrobeStatus.LISTED, item.getStatus());
            assertSame(mapped, item.getDonationRequest());
            verify(donationRequestRepository).save(mapped);
        }

        @Test
        void eventNotFound_throwsNotFound() {
            DonationRequestReq req = new DonationRequestReq(UUID.randomUUID(), "desc", List.of(UUID.randomUUID()));
            when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
            when(donationEventRepository.findById(req.getDonationEventId())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> service.createDonationRequest(req, userDetails));
        }

        @Test
        void eventCancelled_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("owner"));
            DonationEvent event = buildEvent(EventStatus.CANCELLED, org);
            DonationRequestReq req = new DonationRequestReq(event.getId(), "desc", List.of(UUID.randomUUID()));

            when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
            when(donationEventRepository.findById(event.getId())).thenReturn(Optional.of(event));

            assertThrows(BadRequestException.class, () -> service.createDonationRequest(req, userDetails));
        }

        @Test
        void eventCompleted_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("owner"));
            DonationEvent event = buildEvent(EventStatus.COMPLETED, org);
            DonationRequestReq req = new DonationRequestReq(event.getId(), "desc", List.of(UUID.randomUUID()));

            when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
            when(donationEventRepository.findById(event.getId())).thenReturn(Optional.of(event));

            assertThrows(BadRequestException.class, () -> service.createDonationRequest(req, userDetails));
        }

        @Test
        void nullItemIds_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("owner"));
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);
            DonationRequestReq req = new DonationRequestReq(event.getId(), "desc", null);

            when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
            when(donationEventRepository.findById(event.getId())).thenReturn(Optional.of(event));

            assertThrows(BadRequestException.class, () -> service.createDonationRequest(req, userDetails));
        }

        @Test
        void emptyItemIds_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("owner"));
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);
            DonationRequestReq req = new DonationRequestReq(event.getId(), "desc", List.of());

            when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
            when(donationEventRepository.findById(event.getId())).thenReturn(Optional.of(event));

            assertThrows(BadRequestException.class, () -> service.createDonationRequest(req, userDetails));
        }

        @Test
        void someItemsMissing_throwsNotFound() {
            OrganizationDetail org = buildOrg(buildUser("owner"));
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            WardrobeItem found = buildItem(buildUser("alice"), WardrobeStatus.OWNED);
            found.setId(id1);

            DonationRequestReq req = new DonationRequestReq(event.getId(), "desc", List.of(id1, id2));
            when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
            when(donationEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
            when(wardrobeItemRepository.findAllById(List.of(id1, id2))).thenReturn(List.of(found));

            assertThrows(NotFoundException.class, () -> service.createDonationRequest(req, userDetails));
        }

        @Test
        void itemNotOwned_throwsBadRequest() {
            User user = buildUser("alice");
            OrganizationDetail org = buildOrg(buildUser("owner"));
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);
            UUID itemId = UUID.randomUUID();
            WardrobeItem item = buildItem(user, WardrobeStatus.SOLD);

            DonationRequestReq req = new DonationRequestReq(event.getId(), "desc", List.of(itemId));
            when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
            when(donationEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
            when(wardrobeItemRepository.findAllById(List.of(itemId))).thenReturn(List.of(item));

            assertThrows(BadRequestException.class, () -> service.createDonationRequest(req, userDetails));
        }

        @Test
        void itemNotOwnedByCallingUser_throwsBadRequest() {
            User owner = buildUser("alice");
            OrganizationDetail org = buildOrg(buildUser("orgOwner"));
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);
            UUID itemId = UUID.randomUUID();
            WardrobeItem item = buildItem(owner, WardrobeStatus.OWNED);

            DonationRequestReq req = new DonationRequestReq(event.getId(), "desc", List.of(itemId));
            when(donationRequestMapper.toEntity(req)).thenReturn(new DonationRequest());
            when(donationEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
            when(wardrobeItemRepository.findAllById(List.of(itemId))).thenReturn(List.of(item));
            when(userDetails.getUsername()).thenReturn("bob");

            assertThrows(BadRequestException.class, () -> service.createDonationRequest(req, userDetails));
        }
    }

    // ==================== createDonationRequest(DonationRequestCustomReq) ====================

    @Nested
    class CreateDonationRequestCustomReq {

        @Test
        void success() {
            User user = buildUser("alice");
            OrganizationDetail org = buildOrg(buildUser("owner"));
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);
            DonationRequestCustomReq req = new DonationRequestCustomReq(
                    event.getId(), "desc", "item", "cat", "new", "note", null);

            UploadRes uploadRes = new UploadRes("http://img", "pub1");
            WardrobeItem mappedItem = new WardrobeItem();
            DonationRequest mappedRequest = new DonationRequest();
            mappedRequest.setImages(new ArrayList<>());

            when(userDetails.getUsername()).thenReturn("alice");
            when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
            when(uploadService.uploadImage(req.getImage(), "alice")).thenReturn(uploadRes);
            when(wardrobeItemMapper.toEntity(req)).thenReturn(mappedItem);
            when(donationEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
            when(donationRequestMapper.toEntity(req)).thenReturn(mappedRequest);

            DonationRequest result = service.createDonationRequest(req, userDetails);

            assertEquals(DonationStatus.PENDING, result.getStatus());
            assertEquals(user, result.getUser());
            assertEquals(org, result.getOrganizationDetail());
            assertTrue(result.getImages().contains("http://img"));
            assertTrue(result.getItems().contains(mappedItem));
            assertEquals("http://img", mappedItem.getImageUrl());
            verify(wardrobeItemRepository).save(mappedItem);
            verify(donationRequestRepository).save(mappedRequest);
        }

        @Test
        void userNotFound_throwsNotFound() {
            DonationRequestCustomReq req = new DonationRequestCustomReq(
                    UUID.randomUUID(), "desc", "item", "cat", "new", "note", null);
            when(userDetails.getUsername()).thenReturn("ghost");
            when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> service.createDonationRequest(req, userDetails));
        }

        @Test
        void eventNotFound_throwsNotFound() {
            User user = buildUser("alice");
            DonationRequestCustomReq req = new DonationRequestCustomReq(
                    UUID.randomUUID(), "desc", "item", "cat", "new", "note", null);

            when(userDetails.getUsername()).thenReturn("alice");
            when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
            when(uploadService.uploadImage(any(), eq("alice"))).thenReturn(new UploadRes("url", "id"));
            when(wardrobeItemMapper.toEntity(req)).thenReturn(new WardrobeItem());
            when(donationEventRepository.findById(req.getDonationEventId())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> service.createDonationRequest(req, userDetails));
        }

        @Test
        void eventCancelled_throwsBadRequest() {
            User user = buildUser("alice");
            OrganizationDetail org = buildOrg(buildUser("owner"));
            DonationEvent event = buildEvent(EventStatus.CANCELLED, org);
            DonationRequestCustomReq req = new DonationRequestCustomReq(
                    event.getId(), "desc", "item", "cat", "new", "note", null);

            when(userDetails.getUsername()).thenReturn("alice");
            when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
            when(uploadService.uploadImage(any(), eq("alice"))).thenReturn(new UploadRes("url", "id"));
            when(wardrobeItemMapper.toEntity(req)).thenReturn(new WardrobeItem());
            when(donationEventRepository.findById(event.getId())).thenReturn(Optional.of(event));

            assertThrows(BadRequestException.class, () -> service.createDonationRequest(req, userDetails));
        }

        @Test
        void eventCompleted_throwsBadRequest() {
            User user = buildUser("alice");
            OrganizationDetail org = buildOrg(buildUser("owner"));
            DonationEvent event = buildEvent(EventStatus.COMPLETED, org);
            DonationRequestCustomReq req = new DonationRequestCustomReq(
                    event.getId(), "desc", "item", "cat", "new", "note", null);

            when(userDetails.getUsername()).thenReturn("alice");
            when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
            when(uploadService.uploadImage(any(), eq("alice"))).thenReturn(new UploadRes("url", "id"));
            when(wardrobeItemMapper.toEntity(req)).thenReturn(new WardrobeItem());
            when(donationEventRepository.findById(event.getId())).thenReturn(Optional.of(event));

            assertThrows(BadRequestException.class, () -> service.createDonationRequest(req, userDetails));
        }
    }

    // ==================== accept ====================

    @Nested
    class Accept {

        @Test
        void success() {
            User orgOwner = buildUser("org1");
            OrganizationDetail org = buildOrg(orgOwner);
            User donor = buildUser("alice");
            DonationRequest dr = buildDonationRequest(donor, org, DonationStatus.PENDING);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("org1");

            DonationRequest result = service.accept(dr.getId(), userDetails);

            assertEquals(DonationStatus.ACCEPTED, result.getStatus());
            assertNotNull(result.getAcceptedAt());
            verify(donationRequestRepository).save(dr);
            verify(eventPublisher).publishEvent(any(DonationNotificationEvent.class));
        }

        @Test
        void notFound_throwsNotFound() {
            UUID id = UUID.randomUUID();
            when(donationRequestRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> service.accept(id, userDetails));
        }

        @Test
        void notOrgOwner_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.PENDING);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("hacker");

            assertThrows(BadRequestException.class, () -> service.accept(dr.getId(), userDetails));
        }

        @Test
        void notPending_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.ACCEPTED);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("org1");

            assertThrows(BadRequestException.class, () -> service.accept(dr.getId(), userDetails));
        }
    }

    // ==================== reject ====================

    @Nested
    class Reject {

        @Test
        void success() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.PENDING);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("org1");

            DonationRequest result = service.reject(dr.getId(), "khong hop le", userDetails);

            assertEquals(DonationStatus.REJECTED, result.getStatus());
            assertEquals("khong hop le", result.getRejectedReason());
            assertNotNull(result.getUpdatedAt());
            verify(donationRequestRepository).save(dr);
            verify(eventPublisher).publishEvent(any(DonationNotificationEvent.class));
        }

        @Test
        void notFound_throwsNotFound() {
            UUID id = UUID.randomUUID();
            when(donationRequestRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> service.reject(id, "reason", userDetails));
        }

        @Test
        void notOrgOwner_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.PENDING);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("hacker");

            assertThrows(BadRequestException.class, () -> service.reject(dr.getId(), "reason", userDetails));
        }

        @Test
        void notPending_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.REJECTED);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("org1");

            assertThrows(BadRequestException.class, () -> service.reject(dr.getId(), "reason", userDetails));
        }
    }

    // ==================== shipping ====================

    @Nested
    class Shipping {

        @Test
        void success() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.ACCEPTED);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("alice");

            DonationRequest result = service.shipping(dr.getId(), userDetails);

            assertEquals(DonationStatus.SHIPPING, result.getStatus());
            assertNotNull(result.getUpdatedAt());
            verify(donationRequestRepository).save(dr);
        }

        @Test
        void notFound_throwsNotFound() {
            UUID id = UUID.randomUUID();
            when(donationRequestRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> service.shipping(id, userDetails));
        }

        @Test
        void notOwner_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.ACCEPTED);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("bob");

            assertThrows(BadRequestException.class, () -> service.shipping(dr.getId(), userDetails));
        }

        @Test
        void notAccepted_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.PENDING);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("alice");

            assertThrows(BadRequestException.class, () -> service.shipping(dr.getId(), userDetails));
        }
    }

    // ==================== shipped ====================

    @Nested
    class Shipped {

        private ShippingReq req() {
            return new ShippingReq("TRACK123", null);
        }

        @Test
        void success() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.SHIPPING);
            ShippingReq req = req();

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("alice");
            when(uploadService.uploadImage(req.getShippingProofFile(), "alice"))
                    .thenReturn(new UploadRes("proof-url", "id"));

            DonationRequest result = service.shipped(dr.getId(), req, userDetails);

            assertEquals(DonationStatus.SHIPPED, result.getStatus());
            assertEquals("TRACK123", result.getTrackingCode());
            assertEquals("proof-url", result.getShippingProofUrl());
            assertNotNull(result.getShippedAt());
            verify(donationRequestRepository).save(dr);
        }

        @Test
        void notFound_throwsNotFound() {
            UUID id = UUID.randomUUID();
            when(donationRequestRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> service.shipped(id, req(), userDetails));
        }

        @Test
        void notOwner_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.SHIPPING);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("bob");

            assertThrows(BadRequestException.class, () -> service.shipped(dr.getId(), req(), userDetails));
        }

        @Test
        void notShipping_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.ACCEPTED);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("alice");

            assertThrows(BadRequestException.class, () -> service.shipped(dr.getId(), req(), userDetails));
        }
    }

    // ==================== received ====================

    @Nested
    class Received {

        private ReceivedReq req() {
            return new ReceivedReq(null);
        }

        @Test
        void successFromShipped() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.SHIPPED);
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);
            event.setCurrentQuantity(2);
            dr.setDonationEvent(event);
            WardrobeItem item = buildItem(buildUser("alice"), WardrobeStatus.LISTED);
            dr.getItems().add(item);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("org1");
            when(uploadService.uploadImage(any(), eq("org1"))).thenReturn(new UploadRes("receipt-url", "id"));

            DonationRequest result = service.received(dr.getId(), req(), userDetails);

            assertEquals(DonationStatus.RECEIVED, result.getStatus());
            assertEquals("receipt-url", result.getReceiptProofUrl());
            assertEquals(3, event.getCurrentQuantity());
            assertEquals(WardrobeStatus.DONATED, item.getStatus());
            verify(donationRequestRepository).save(dr);
            verify(donationEventRepository).save(event);
            verify(wardrobeItemRepository).save(item);
            verify(eventPublisher).publishEvent(any(DonationNotificationEvent.class));
        }

        @Test
        void successFromShipping() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.SHIPPING);
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);
            dr.setDonationEvent(event);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("org1");
            when(uploadService.uploadImage(any(), eq("org1"))).thenReturn(new UploadRes("receipt-url", "id"));

            DonationRequest result = service.received(dr.getId(), req(), userDetails);

            assertEquals(DonationStatus.RECEIVED, result.getStatus());
        }

        @Test
        void notFound_throwsNotFound() {
            UUID id = UUID.randomUUID();
            when(donationRequestRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> service.received(id, req(), userDetails));
        }

        @Test
        void notOrgOwner_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.SHIPPED);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("hacker");

            assertThrows(BadRequestException.class, () -> service.received(dr.getId(), req(), userDetails));
        }

        @Test
        void invalidStatus_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.ACCEPTED);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("org1");

            assertThrows(BadRequestException.class, () -> service.received(dr.getId(), req(), userDetails));
        }
    }

    // ==================== completed ====================

    @Nested
    class Completed {

        @Test
        void fromReceived_success() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.RECEIVED);
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);
            event.setCurrentQuantity(1);
            dr.setDonationEvent(event);
            WardrobeItem item = buildItem(buildUser("alice"), WardrobeStatus.LISTED);
            dr.getItems().add(item);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));

            DonationRequest result = service.completed(dr.getId());

            assertEquals(DonationStatus.COMPLETED, result.getStatus());
            assertNotNull(result.getCompletedAt());
            assertEquals(WardrobeStatus.DONATED, item.getStatus());
            assertEquals(2, event.getCurrentQuantity());
            verify(donationRequestRepository).save(dr);
            verify(wardrobeItemRepository).save(item);
        }

        @Test
        void fromShipped_after10Days_success() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.SHIPPED);
            dr.setShippedAt(LocalDateTime.now().minusDays(11));
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);
            dr.setDonationEvent(event);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));

            DonationRequest result = service.completed(dr.getId());

            assertEquals(DonationStatus.COMPLETED, result.getStatus());
            verify(donationRequestRepository).save(dr);
        }

        @Test
        void fromShipped_shippedAtNull_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.SHIPPED);
            dr.setShippedAt(null);
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);
            dr.setDonationEvent(event);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));

            assertThrows(BadRequestException.class, () -> service.completed(dr.getId()));
        }

        @Test
        void fromShipped_before10Days_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.SHIPPED);
            dr.setShippedAt(LocalDateTime.now().minusDays(1));
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);
            dr.setDonationEvent(event);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));

            assertThrows(BadRequestException.class, () -> service.completed(dr.getId()));
        }

        @Test
        void invalidStatus_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.PENDING);
            DonationEvent event = buildEvent(EventStatus.ONGOING, org);
            dr.setDonationEvent(event);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));

            assertThrows(BadRequestException.class, () -> service.completed(dr.getId()));
        }

        @Test
        void notFound_throwsNotFound() {
            UUID id = UUID.randomUUID();
            when(donationRequestRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> service.completed(id));
        }
    }

    // ==================== cancel ====================

    @Nested
    class Cancel {

        @Test
        void fromPending_success() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.PENDING);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("alice");

            DonationRequest result = service.cancel(dr.getId(), "doi y", userDetails);

            assertEquals(DonationStatus.CANCELLED, result.getStatus());
            assertEquals("doi y", result.getCancelReason());
            assertNotNull(result.getCanceledAt());
            verify(donationRequestRepository).save(dr);
        }

        @Test
        void fromAccepted_success() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.ACCEPTED);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("alice");

            DonationRequest result = service.cancel(dr.getId(), "doi y", userDetails);

            assertEquals(DonationStatus.CANCELLED, result.getStatus());
        }

        @Test
        void notFound_throwsNotFound() {
            UUID id = UUID.randomUUID();
            when(donationRequestRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> service.cancel(id, "reason", userDetails));
        }

        @Test
        void notOwner_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.PENDING);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("bob");

            assertThrows(BadRequestException.class, () -> service.cancel(dr.getId(), "reason", userDetails));
        }

        @Test
        void invalidStatus_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.SHIPPED);

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(userDetails.getUsername()).thenReturn("alice");

            assertThrows(BadRequestException.class, () -> service.cancel(dr.getId(), "reason", userDetails));
        }
    }

    // ==================== assignOrganization ====================

    @Nested
    class AssignOrganization {

        @Test
        void success() {
            OrganizationDetail oldOrg = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), oldOrg, DonationStatus.PENDING);
            dr.setCreatedAt(LocalDateTime.now().minusDays(6));
            OrganizationDetail newOrg = buildOrg(buildUser("org2"));

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(organizationDetailRepository.findById(newOrg.getId())).thenReturn(Optional.of(newOrg));

            DonationRequest result = service.assignOrganization(dr.getId(), newOrg.getId());

            assertEquals(newOrg, result.getOrganizationDetail());
            assertNotNull(result.getUpdatedAt());
            verify(donationRequestRepository).save(dr);
        }

        @Test
        void donationNotFound_throwsNotFound() {
            UUID id = UUID.randomUUID();
            when(donationRequestRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> service.assignOrganization(id, UUID.randomUUID()));
        }

        @Test
        void notPending_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.ACCEPTED);
            dr.setCreatedAt(LocalDateTime.now().minusDays(6));

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));

            assertThrows(BadRequestException.class,
                    () -> service.assignOrganization(dr.getId(), UUID.randomUUID()));
        }

        @Test
        void notOldEnough_throwsBadRequest() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.PENDING);
            dr.setCreatedAt(LocalDateTime.now().minusDays(1));

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));

            assertThrows(BadRequestException.class,
                    () -> service.assignOrganization(dr.getId(), UUID.randomUUID()));
        }

        @Test
        void organizationNotFound_throwsNotFound() {
            OrganizationDetail org = buildOrg(buildUser("org1"));
            DonationRequest dr = buildDonationRequest(buildUser("alice"), org, DonationStatus.PENDING);
            dr.setCreatedAt(LocalDateTime.now().minusDays(6));
            UUID newOrgId = UUID.randomUUID();

            when(donationRequestRepository.findById(dr.getId())).thenReturn(Optional.of(dr));
            when(organizationDetailRepository.findById(newOrgId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> service.assignOrganization(dr.getId(), newOrgId));
        }
    }

    // ==================== checkPendingDonations ====================

    @Test
    void checkPendingDonations_callsRepositoryAndNotifies() {
        List<DonationRequest> overdue = List.of(new DonationRequest());
        when(donationRequestRepository.findPendingDonationsOverdue(eq(DonationStatus.PENDING), any()))
                .thenReturn(overdue);

        service.checkPendingDonations();

        verify(notificationAdminService).notifyAdminPendingOverdue(overdue);
    }

    // ==================== getPendingDonations ====================

    @Nested
    class GetPendingDonations {

        @Test
        void success() {
            DonationRequest dr = new DonationRequest();
            when(donationRequestRepository.findPendingDonationsOverdue(eq(DonationStatus.PENDING), any()))
                    .thenReturn(List.of(dr));
            when(donationRequestMapper.toPendingResponse(dr)).thenReturn(new DonationPendingResponse());

            List<DonationPendingResponse> result = service.getPendingDonations(userDetails);

            assertEquals(1, result.size());
        }

        @Test
        void empty_throwsNotFound() {
            when(donationRequestRepository.findPendingDonationsOverdue(eq(DonationStatus.PENDING), any()))
                    .thenReturn(List.of());

            assertThrows(NotFoundException.class, () -> service.getPendingDonations(userDetails));
        }
    }

    // ==================== getAllDonationRequestsFromOrganizationId ====================

    @Nested
    class GetAllFromOrganization {

        @Test
        void success() {
            UUID orgId = UUID.randomUUID();
            DonationRequest dr = new DonationRequest();
            when(donationRequestRepository.findByOrganizationDetail_Id(orgId)).thenReturn(List.of(dr));
            when(donationRequestMapper.toResponse(dr)).thenReturn(new DonationRequestResponse());

            List<DonationRequestResponse> result = service.getAllDonationRequestsFromOrganizationId(orgId);

            assertEquals(1, result.size());
        }

        @Test
        void empty_throwsNotFound() {
            UUID orgId = UUID.randomUUID();
            when(donationRequestRepository.findByOrganizationDetail_Id(orgId)).thenReturn(List.of());

            assertThrows(NotFoundException.class,
                    () -> service.getAllDonationRequestsFromOrganizationId(orgId));
        }
    }

    // ==================== getAllDonationRequestsFromUser ====================

    @Nested
    class GetAllFromUser {

        @Test
        void success() {
            User user = buildUser("alice");
            DonationRequest dr = new DonationRequest();

            when(userDetails.getUsername()).thenReturn("alice");
            when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
            when(donationRequestRepository.findByUser_UserId(user.getUserId())).thenReturn(List.of(dr));
            when(donationRequestMapper.toResponse(dr)).thenReturn(new DonationRequestResponse());

            List<DonationRequestResponse> result = service.getAllDonationRequestsFromUser(userDetails);

            assertEquals(1, result.size());
        }

        @Test
        void emptyList_returnsEmpty() {
            User user = buildUser("alice");

            when(userDetails.getUsername()).thenReturn("alice");
            when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
            when(donationRequestRepository.findByUser_UserId(user.getUserId())).thenReturn(List.of());

            List<DonationRequestResponse> result = service.getAllDonationRequestsFromUser(userDetails);

            assertTrue(result.isEmpty());
        }

        @Test
        void userNotFound_throwsNotFound() {
            when(userDetails.getUsername()).thenReturn("ghost");
            when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> service.getAllDonationRequestsFromUser(userDetails));
        }
    }

    // ==================== autoCheckReceivedDonations ====================

    @Nested
    class AutoCheckReceivedDonations {

        @Test
        void reminderNull_sendsNotificationAndSetsReminder() {
            User user = buildUser("alice");
            DonationRequest dr = new DonationRequest();
            dr.setId(UUID.randomUUID());
            dr.setUser(user);
            dr.setReminderSentAt(null);

            when(donationRequestRepository.findByStatusAndUpdatedAtBefore(eq(DonationStatus.SHIPPING), any()))
                    .thenReturn(List.of(dr));

            service.autoCheckReceivedDonations();

            assertNotNull(dr.getReminderSentAt());
            verify(eventPublisher).publishEvent(any(DonationNotificationEvent.class));
            verify(donationRequestRepository).save(dr);
        }

        @Test
        void reminderOldEnough_sendsNotificationAgain() {
            User user = buildUser("alice");
            DonationRequest dr = new DonationRequest();
            dr.setId(UUID.randomUUID());
            dr.setUser(user);
            dr.setReminderSentAt(LocalDateTime.now().minusDays(4));

            when(donationRequestRepository.findByStatusAndUpdatedAtBefore(eq(DonationStatus.SHIPPING), any()))
                    .thenReturn(List.of(dr));

            service.autoCheckReceivedDonations();

            verify(eventPublisher).publishEvent(any(DonationNotificationEvent.class));
            verify(donationRequestRepository).save(dr);
        }

        @Test
        void reminderRecent_doesNotSendAgain() {
            User user = buildUser("alice");
            DonationRequest dr = new DonationRequest();
            dr.setId(UUID.randomUUID());
            dr.setUser(user);
            dr.setReminderSentAt(LocalDateTime.now().minusDays(1));

            when(donationRequestRepository.findByStatusAndUpdatedAtBefore(eq(DonationStatus.SHIPPING), any()))
                    .thenReturn(List.of(dr));

            service.autoCheckReceivedDonations();

            verify(eventPublisher, never()).publishEvent(any());
            verify(donationRequestRepository, never()).save(any());
        }

        @Test
        void emptyList_doesNothing() {
            when(donationRequestRepository.findByStatusAndUpdatedAtBefore(eq(DonationStatus.SHIPPING), any()))
                    .thenReturn(List.of());

            service.autoCheckReceivedDonations();

            verify(eventPublisher, never()).publishEvent(any());
        }
    }
}