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

        donationEventReq = new DonationEventReq();
        donationEventReq.setTitle("New Event");
    }

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
        verify(donationEventRepository, never()).save(any());
    }

    @Test
    void updateDonationEvent_ShouldUpdateFields_WhenEventExists() {
        donationEventReq.setTitle("Updated Title");
        donationEventReq.setDescription("Updated Desc");
        DonationEventLogRes logRes = new DonationEventLogRes();

        when(donationEventRepository.findById(eventId)).thenReturn(Optional.of(donationEvent));
        when(donationEventRepository.save(any(DonationEvent.class))).thenReturn(donationEvent);
        when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

        DonationEventLogRes result = donationEventService.updateDonationEvent(eventId, donationEventReq, userDetails);

        assertNotNull(result);
        assertEquals("Updated Title", donationEvent.getTitle());
        assertEquals("Updated Desc", donationEvent.getDescription());
        verify(donationEventRepository, times(1)).save(donationEvent);
    }

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
    void getAllByFilter_ShouldReturnFilteredList() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setItemType("CLOTHES");

        donationEvent.setAcceptedTypes(Arrays.asList("CLOTHES", "BOOKS"));
        
        when(donationEventRepository.findAll()).thenReturn(Collections.singletonList(donationEvent));
        when(donationEventMapper.toResponse(any(DonationEvent.class))).thenReturn(new DonationEventRes());

        List<DonationEventRes> results = donationEventService.getAllByFilter(filter);

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void getAllByFilter_ShouldThrowBadRequest_WhenDistanceFilterIsIncomplete() {
        DonationEventFilterReq filter = new DonationEventFilterReq();
        filter.setLatitude(BigDecimal.valueOf(10.0));
        // Missing longitude and maxDistanceKm

        assertThrows(BadRequestException.class, () -> donationEventService.getAllByFilter(filter));
    }

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
    void cancelDonationEvent_ShouldThrowIllegalState_WhenEventAlreadyCompleted() {
        donationEvent.setStatus(EventStatus.COMPLETED);

        when(donationEventRepository.findById(eventId)).thenReturn(Optional.of(donationEvent));

        assertThrows(IllegalStateException.class, () -> donationEventService.cancelDonationEvent(eventId, userDetails));
    }
    
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
}
