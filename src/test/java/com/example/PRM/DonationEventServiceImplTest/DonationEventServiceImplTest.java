package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.donationEvent.DonationEventFilterReq;
import com.example.PRM.dto.request.donationEvent.DonationEventReq;
import com.example.PRM.dto.response.donationEvent.DonationEventLogRes;
import com.example.PRM.dto.response.donationEvent.DonationEventRes;
import com.example.PRM.entity.DonationEvent;
import com.example.PRM.entity.OrganizationDetail;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.DonationEventMapper;
import com.example.PRM.repository.DonationEventRepository;
import com.example.PRM.repository.OrganizationDetailRepository;
import com.example.PRM.status_enum.EventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationEventServiceImplTest {

    @InjectMocks
    private DonationEventServiceImpl donationEventService;

    @Mock
    private DonationEventRepository donationEventRepository;
    @Mock
    private DonationEventMapper donationEventMapper;
    @Mock
    private OrganizationDetailRepository organizationDetailRepository;
    @Mock
    private UserDetails userDetails;

    private DonationEvent donationEvent;
    private DonationEventReq donationEventReq;
    private OrganizationDetail organizationDetail;
    private UUID eventId;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        orgId = UUID.randomUUID();

        organizationDetail = new OrganizationDetail();
        organizationDetail.setId(orgId);

        donationEvent = new DonationEvent();
        donationEvent.setId(eventId);
        donationEvent.setOrganizationDetail(organizationDetail);
        donationEvent.setStatus(EventStatus.UPCOMING);
        donationEvent.setTitle("Old Title");
        donationEvent.setAcceptedTypes(Arrays.asList("CLOTHES"));

        donationEventReq = new DonationEventReq();
        donationEventReq.setTitle("New Title");
    }

    // CREATE DONATION EVENT
    @Test
    void createDonationEvent_ShouldReturnLogRes_WhenOrgExists() {
        DonationEventLogRes logRes = new DonationEventLogRes();
        
        when(donationEventMapper.toEntity(donationEventReq)).thenReturn(donationEvent);
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
        when(donationEventRepository.save(any(DonationEvent.class))).thenReturn(donationEvent);
        when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

        DonationEventLogRes result = donationEventService.createDonationEvent(donationEventReq, orgId, userDetails);

        assertNotNull(result);
        verify(donationEventRepository, times(1)).save(donationEvent);
    }

    @Test
    void createDonationEvent_ShouldThrowNotFound_WhenOrgDoesNotExist() {
        when(donationEventMapper.toEntity(donationEventReq)).thenReturn(donationEvent);
        when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> 
            donationEventService.createDonationEvent(donationEventReq, orgId, userDetails));
    }

    // UPDATE DONATION EVENT
    @Test
    void updateDonationEvent_ShouldUpdateAllFields_WhenEventExists() {
        donationEventReq.setTitle("Updated Title");
        donationEventReq.setDescription("Updated Desc");
        donationEventReq.setStartDate(OffsetDateTime.now());
        donationEventReq.setEndDate(OffsetDateTime.now().plusDays(1));
        donationEventReq.setLatitude(BigDecimal.valueOf(10.0));
        donationEventReq.setLongitude(BigDecimal.valueOf(106.0));
        donationEventReq.setAcceptedTypes(Arrays.asList("BOOKS", "CLOTHES"));
        donationEventReq.setTargetQuantity(100);
        donationEventReq.setStatus(EventStatus.ONGOING);
        donationEventReq.setBannerUrl("banner.jpg");

        DonationEventLogRes logRes = new DonationEventLogRes();

        when(donationEventRepository.findById(eventId)).thenReturn(Optional.of(donationEvent));
        when(donationEventRepository.save(any(DonationEvent.class))).thenReturn(donationEvent);
        when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

        DonationEventLogRes result = donationEventService.updateDonationEvent(eventId, donationEventReq, userDetails);

        assertNotNull(result);
        assertEquals("Updated Title", donationEvent.getTitle());
        assertEquals("Updated Desc", donationEvent.getDescription());
        assertEquals(BigDecimal.valueOf(10.0), donationEvent.getLatitude());
        assertEquals(EventStatus.ONGOING, donationEvent.getStatus());
        assertEquals("banner.jpg", donationEvent.getBannerUrl());
        verify(donationEventRepository, times(1)).save(donationEvent);
    }

    @Test
    void updateDonationEvent_ShouldIgnoreNullFields_WhenEventExists() {
        DonationEventReq req = new DonationEventReq();
        req.setAcceptedTypes(Collections.emptyList());
        
        DonationEventLogRes logRes = new DonationEventLogRes();
        when(donationEventRepository.findById(eventId)).thenReturn(Optional.of(donationEvent));
        when(donationEventRepository.save(any(DonationEvent.class))).thenReturn(donationEvent);
        when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

        DonationEventLogRes result = donationEventService.updateDonationEvent(eventId, req, userDetails);
        assertNotNull(result);
    }

    @Test
    void updateDonationEvent_ShouldThrowNotFound_WhenEventDoesNotExist() {
        when(donationEventRepository.findById(eventId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationEventService.updateDonationEvent(eventId, donationEventReq, userDetails));
    }

    // DELETE DONATION EVENT
    @Test
    void deleteDonationEvent_ShouldDelete_WhenEventExists() {
        DonationEventLogRes logRes = new DonationEventLogRes();

        when(donationEventRepository.findById(eventId)).thenReturn(Optional.of(donationEvent));
        when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

        DonationEventLogRes result = donationEventService.deleteDonationEvent(eventId, userDetails);

        assertNotNull(result);
        verify(donationEventRepository, times(1)).delete(donationEvent);
    }

    @Test
    void deleteDonationEvent_ShouldThrowNotFound_WhenEventDoesNotExist() {
        when(donationEventRepository.findById(eventId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationEventService.deleteDonationEvent(eventId, userDetails));
    }

    // GET ALL DONATION EVENTS
    @Test
    void getAllDonationEvents_ShouldReturnList() {
        when(donationEventRepository.findAll()).thenReturn(Collections.singletonList(donationEvent));
        when(donationEventMapper.toResponse(any())).thenReturn(new DonationEventRes());

        List<DonationEventRes> res = donationEventService.getAllDonationEvents();
        assertEquals(1, res.size());
    }

    // GET ALL BY ORG ID
    @Test
    void getAllByOrgId_ShouldReturnList() {
        when(donationEventRepository.findByOrganizationDetail_Id(orgId)).thenReturn(Collections.singletonList(donationEvent));
        when(donationEventMapper.toResponse(any())).thenReturn(new DonationEventRes());

        List<DonationEventRes> res = donationEventService.getAllByOrgId(orgId);
        assertEquals(1, res.size());
    }

    // GET ALL BY FILTER
    @Test
    void getAllByFilter_ShouldReturnFilteredList_ByItemType() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setItemType("CLOTHES");

        donationEvent.setAcceptedTypes(Arrays.asList("CLOTHES", "BOOKS"));
        
        when(donationEventRepository.findAll()).thenReturn(Collections.singletonList(donationEvent));
        when(donationEventMapper.toResponse(any(DonationEvent.class))).thenReturn(new DonationEventRes());

        List<DonationEventRes> results = donationEventService.getAllByFilter(filter);

        assertEquals(1, results.size());
    }

    @Test
    void getAllByFilter_ShouldExclude_ByItemType() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setItemType("TOYS");
        
        when(donationEventRepository.findAll()).thenReturn(Collections.singletonList(donationEvent));

        List<DonationEventRes> results = donationEventService.getAllByFilter(filter);

        assertEquals(0, results.size());
    }

    @Test
    void getAllByFilter_ShouldReturnFilteredList_ByCity() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setCity("Hanoi");

        donationEvent.setLocation("Hanoi City");
        
        when(donationEventRepository.findAll()).thenReturn(Collections.singletonList(donationEvent));
        when(donationEventMapper.toResponse(any(DonationEvent.class))).thenReturn(new DonationEventRes());

        List<DonationEventRes> results = donationEventService.getAllByFilter(filter);

        assertEquals(1, results.size());
    }

    @Test
    void getAllByFilter_ShouldExclude_ByCity() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setCity("HCMC");

        donationEvent.setLocation("Hanoi City");
        
        when(donationEventRepository.findAll()).thenReturn(Collections.singletonList(donationEvent));

        List<DonationEventRes> results = donationEventService.getAllByFilter(filter);

        assertEquals(0, results.size());
    }

    @Test
    void getAllByFilter_ShouldExclude_ByCity_WhenLocationNull() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setCity("Hanoi");
        donationEvent.setLocation(null);
        when(donationEventRepository.findAll()).thenReturn(Collections.singletonList(donationEvent));
        List<DonationEventRes> results = donationEventService.getAllByFilter(filter);
        assertEquals(0, results.size());
    }

    @Test
    void getAllByFilter_ShouldReturnFilteredList_ByDistance() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setLatitude(BigDecimal.valueOf(10.0));
        filter.setLongitude(BigDecimal.valueOf(106.0));
        filter.setMaxDistanceKm(5.0); // 5km

        donationEvent.setLatitude(BigDecimal.valueOf(10.001)); // very close
        donationEvent.setLongitude(BigDecimal.valueOf(106.001));
        
        when(donationEventRepository.findAll()).thenReturn(Collections.singletonList(donationEvent));
        when(donationEventMapper.toResponse(any(DonationEvent.class))).thenReturn(new DonationEventRes());

        List<DonationEventRes> results = donationEventService.getAllByFilter(filter);

        assertEquals(1, results.size());
    }

    @Test
    void getAllByFilter_ShouldExclude_ByDistance_TooFar() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setLatitude(BigDecimal.valueOf(10.0));
        filter.setLongitude(BigDecimal.valueOf(106.0));
        filter.setMaxDistanceKm(5.0); // 5km

        donationEvent.setLatitude(BigDecimal.valueOf(11.0)); // Far away
        donationEvent.setLongitude(BigDecimal.valueOf(107.0));
        
        when(donationEventRepository.findAll()).thenReturn(Collections.singletonList(donationEvent));

        List<DonationEventRes> results = donationEventService.getAllByFilter(filter);

        assertEquals(0, results.size());
    }

    @Test
    void getAllByFilter_ShouldExclude_ByDistance_WhenEventLatLngNull() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setLatitude(BigDecimal.valueOf(10.0));
        filter.setLongitude(BigDecimal.valueOf(106.0));
        filter.setMaxDistanceKm(5.0); 

        donationEvent.setLatitude(null); 
        donationEvent.setLongitude(null);
        
        when(donationEventRepository.findAll()).thenReturn(Collections.singletonList(donationEvent));
        List<DonationEventRes> results = donationEventService.getAllByFilter(filter);
        assertEquals(0, results.size());
    }

    @Test
    void getAllByFilter_ShouldThrowBadRequest_WhenDistanceFilterIsIncomplete() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setLatitude(BigDecimal.valueOf(10.0));
        // Missing longitude and maxDistanceKm

        assertThrows(BadRequestException.class, () -> donationEventService.getAllByFilter(filter));
    }

    @Test
    void getAllByFilter_ShouldThrowBadRequest_WhenOnlyLongitudeIsSet() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setLongitude(BigDecimal.valueOf(10.0));

        assertThrows(BadRequestException.class, () -> donationEventService.getAllByFilter(filter));
    }

    @Test
    void getAllByFilter_ShouldThrowBadRequest_WhenOnlyMaxDistanceIsSet() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setMaxDistanceKm(10.0);

        assertThrows(BadRequestException.class, () -> donationEventService.getAllByFilter(filter));
    }

    @Test
    void getAllByFilter_ShouldInclude_WhenItemTypeIsBlank() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setItemType("");
        when(donationEventRepository.findAll()).thenReturn(Collections.singletonList(donationEvent));
        when(donationEventMapper.toResponse(any(DonationEvent.class))).thenReturn(new DonationEventRes());

        List<DonationEventRes> results = donationEventService.getAllByFilter(filter);
        assertEquals(1, results.size());
    }

    @Test
    void getAllByFilter_ShouldInclude_WhenCityIsBlank() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setCity("   ");
        when(donationEventRepository.findAll()).thenReturn(Collections.singletonList(donationEvent));
        when(donationEventMapper.toResponse(any(DonationEvent.class))).thenReturn(new DonationEventRes());

        List<DonationEventRes> results = donationEventService.getAllByFilter(filter);
        assertEquals(1, results.size());
    }

    // CANCEL DONATION EVENT
    @Test
    void cancelDonationEvent_ShouldChangeStatusToCancelled_WhenEventExists() {
        DonationEventLogRes logRes = new DonationEventLogRes();

        when(donationEventRepository.findById(eventId)).thenReturn(Optional.of(donationEvent));
        when(donationEventRepository.save(any(DonationEvent.class))).thenReturn(donationEvent);
        when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

        DonationEventLogRes result = donationEventService.cancelDonationEvent(eventId, userDetails);

        assertNotNull(result);
        assertEquals(EventStatus.CANCELLED, donationEvent.getStatus());
        verify(donationEventRepository, times(1)).save(donationEvent);
    }

    @Test
    void cancelDonationEvent_ShouldThrowNotFound_WhenNotExists() {
        when(donationEventRepository.findById(eventId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationEventService.cancelDonationEvent(eventId, userDetails));
    }

    @Test
    void cancelDonationEvent_ShouldThrowIllegalState_WhenEventAlreadyCompleted() {
        donationEvent.setStatus(EventStatus.COMPLETED);
        when(donationEventRepository.findById(eventId)).thenReturn(Optional.of(donationEvent));
        assertThrows(IllegalStateException.class, () -> donationEventService.cancelDonationEvent(eventId, userDetails));
    }
    
    // COMPLETE DONATION EVENT
    @Test
    void completeDonationEvent_ShouldChangeStatusToCompleted_WhenOngoing() {
        donationEvent.setStatus(EventStatus.ONGOING);
        DonationEventLogRes logRes = new DonationEventLogRes();

        when(donationEventRepository.findById(eventId)).thenReturn(Optional.of(donationEvent));
        when(donationEventRepository.save(any(DonationEvent.class))).thenReturn(donationEvent);
        when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

        DonationEventLogRes result = donationEventService.completeDonationEvent(eventId, userDetails);

        assertNotNull(result);
        assertEquals(EventStatus.COMPLETED, donationEvent.getStatus());
        verify(donationEventRepository, times(1)).save(donationEvent);
    }

    @Test
    void completeDonationEvent_ShouldThrowNotFound_WhenNotExists() {
        when(donationEventRepository.findById(eventId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationEventService.completeDonationEvent(eventId, userDetails));
    }

    @Test
    void completeDonationEvent_ShouldThrowIllegalState_WhenNotOngoing() {
        donationEvent.setStatus(EventStatus.UPCOMING);
        when(donationEventRepository.findById(eventId)).thenReturn(Optional.of(donationEvent));
        assertThrows(IllegalStateException.class, () -> donationEventService.completeDonationEvent(eventId, userDetails));
    }

    // ONGOING DONATION EVENT
    @Test
    void ongoingDonationEvent_ShouldChangeStatusToOngoing_WhenUpcoming() {
        donationEvent.setStatus(EventStatus.UPCOMING);
        DonationEventLogRes logRes = new DonationEventLogRes();

        when(donationEventRepository.findById(eventId)).thenReturn(Optional.of(donationEvent));
        when(donationEventRepository.save(any(DonationEvent.class))).thenReturn(donationEvent);
        when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

        DonationEventLogRes result = donationEventService.ongoingDonationEvent(eventId, userDetails);

        assertNotNull(result);
        assertEquals(EventStatus.ONGOING, donationEvent.getStatus());
        verify(donationEventRepository, times(1)).save(donationEvent);
    }

    @Test
    void ongoingDonationEvent_ShouldThrowNotFound_WhenNotExists() {
        when(donationEventRepository.findById(eventId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> donationEventService.ongoingDonationEvent(eventId, userDetails));
    }

    @Test
    void ongoingDonationEvent_ShouldThrowIllegalState_WhenNotUpcoming() {
        donationEvent.setStatus(EventStatus.ONGOING);
        when(donationEventRepository.findById(eventId)).thenReturn(Optional.of(donationEvent));
        assertThrows(IllegalStateException.class, () -> donationEventService.ongoingDonationEvent(eventId, userDetails));
    }
}
