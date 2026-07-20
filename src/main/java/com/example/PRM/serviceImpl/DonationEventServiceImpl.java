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
import com.example.PRM.service.DonationEventService;
import com.example.PRM.status_enum.EventStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DonationEventServiceImpl implements DonationEventService {
    private final DonationEventRepository donationEventRepository;
    private final DonationEventMapper donationEventMapper;
    private final OrganizationDetailRepository organizationDetailRepository;
    public DonationEventServiceImpl(DonationEventRepository donationEventRepository, DonationEventMapper donationEventMapper, OrganizationDetailRepository organizationDetailRepository) {
        this.donationEventRepository = donationEventRepository;
        this.donationEventMapper = donationEventMapper;
        this.organizationDetailRepository = organizationDetailRepository;
    }
    @Override
    public DonationEventLogRes createDonationEvent(DonationEventReq donationEventReq, UUID orgId, UserDetails userDetails) {
        DonationEvent donationEvent = donationEventMapper.toEntity(donationEventReq);
        OrganizationDetail organizationDetail = organizationDetailRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
        donationEvent.setOrganizationDetail(organizationDetail);
        donationEventRepository.save(donationEvent);

        return donationEventMapper.toResponseLog(donationEvent);
    }

    @Override
    public DonationEventLogRes updateDonationEvent(UUID donationEventId, DonationEventReq donationEventReq,
                                    UserDetails userDetails) {
        DonationEvent donationEvent = donationEventRepository.findById(donationEventId)
                .orElseThrow(() -> new NotFoundException(
                        "Donation event not found with id: " + donationEventId));

        if (donationEventReq.getTitle() != null)
            donationEvent.setTitle(donationEventReq.getTitle());
        if (donationEventReq.getDescription() != null)
            donationEvent.setDescription(donationEventReq.getDescription());
        if (donationEventReq.getStartDate() != null)
            donationEvent.setStartDate(donationEventReq.getStartDate());
        if (donationEventReq.getEndDate() != null)
            donationEvent.setEndDate(donationEventReq.getEndDate());
        if (donationEventReq.getLatitude() != null)
            donationEvent.setLatitude(donationEventReq.getLatitude());
        if (donationEventReq.getLongitude() != null)
            donationEvent.setLongitude(donationEventReq.getLongitude());
        if (donationEventReq.getAcceptedTypes() != null)
            donationEvent.setAcceptedTypes(donationEventReq.getAcceptedTypes());
        if (donationEventReq.getTargetQuantity() != null)
            donationEvent.setTargetQuantity(donationEventReq.getTargetQuantity());
        if (donationEventReq.getStatus() != null)
            donationEvent.setStatus(donationEventReq.getStatus());
        if (donationEventReq.getAcceptedTypes() != null && !donationEventReq.getAcceptedTypes().isEmpty())
            donationEvent.getOrganizationDetail().setAcceptedTypes(donationEventReq.getAcceptedTypes());
        if (donationEventReq.getBannerUrl() != null)
            donationEvent.setBannerUrl(donationEventReq.getBannerUrl());

        donationEventRepository.save(donationEvent);

        return donationEventMapper.toResponseLog(donationEvent);
    }

    @Override
    public DonationEventLogRes deleteDonationEvent(UUID donationEventId,
                                    UserDetails userDetails) {
        DonationEvent donationEvent = donationEventRepository.findById(donationEventId)
                .orElseThrow(() -> new NotFoundException(
                        "Donation event not found with id: " + donationEventId));

        donationEventRepository.delete(donationEvent);

        return donationEventMapper.toResponseLog(donationEvent);
    }
    @Override
    public List<DonationEventRes> getAllDonationEvents() {
        List<DonationEvent> donationEvents = donationEventRepository.findAll();
        return donationEvents.stream().map(donationEventMapper::toResponse).toList();
    }

    @Override
    public List<DonationEventRes> getAllByFilter(DonationEventFilterReq req) {

        validateDistanceFilter(req);

        List<DonationEvent> events = donationEventRepository.findAll();

        return events.stream()

                // ===== FILTER ITEM TYPE (in DonationEvent) =====
                .filter(event -> {

                    if (req.getItemType() == null || req.getItemType().isBlank()) {
                        return true;
                    }

                    List<String> acceptedTypes = event.getAcceptedTypes();

                    return acceptedTypes != null
                            && acceptedTypes.contains(req.getItemType());
                })

                // ===== FILTER CITY (using event address only if you have it) =====
                .filter(event -> {

                    if (req.getCity() == null || req.getCity().isBlank()) {
                        return true;
                    }

                    String location = event.getLocation(); // hoặc address nếu bạn dùng field đó

                    return location != null
                            && location.toLowerCase()
                            .contains(req.getCity().toLowerCase());
                })

                // ===== FILTER DISTANCE (DonationEvent LAT/LNG) =====
                .filter(event -> {

                    boolean hasDistanceFilter = req.getLatitude() != null; // đã validate: có 1 thì có đủ cả 3

                    if (!hasDistanceFilter) {
                        return true;
                    }

                    if (event.getLatitude() == null || event.getLongitude() == null) {
                        return false;
                    }

                    double distance = calculateDistance(
                            req.getLatitude().doubleValue(),
                            req.getLongitude().doubleValue(),
                            event.getLatitude().doubleValue(),
                            event.getLongitude().doubleValue()
                    );

                    return distance <= req.getMaxDistanceKm();
                })

                .map(donationEventMapper::toResponse)
                .toList();
    }

    @Override
    public List<DonationEventRes> getAllByOrgId(UUID orgId) {
        List<DonationEvent> lists = donationEventRepository.findByOrganizationDetail_Id(orgId);
        return lists.stream().map(donationEventMapper::toResponse).toList();
    }

    @Override
    public DonationEventLogRes cancelDonationEvent(UUID donationEventId, UserDetails userDetails) {
        DonationEvent event = donationEventRepository.findById(donationEventId)
                .orElseThrow(() -> new NotFoundException(
                        "Không tìm thấy donation event với id: " + donationEventId));

        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Không thể hủy sự kiện đang ở trạng thái " + event.getStatus());
        }

        event.setStatus(EventStatus.CANCELLED);
        donationEventRepository.save(event);

        return donationEventMapper.toResponseLog(event);
    }

    @Override
    public DonationEventLogRes completeDonationEvent(UUID donationEventId, UserDetails userDetails) {
        DonationEvent event = donationEventRepository.findById(donationEventId)
                .orElseThrow(() -> new NotFoundException(
                        "Không tìm thấy donation event với id: " + donationEventId));

        if (event.getStatus() != EventStatus.ONGOING) {
            throw new IllegalStateException(
                    "Chỉ có thể hoàn thành sự kiện đang ONGOING, hiện tại: " + event.getStatus());
        }

        event.setStatus(EventStatus.COMPLETED);
        donationEventRepository.save(event);

        return donationEventMapper.toResponseLog(event);
    }

    @Override
    public DonationEventLogRes ongoingDonationEvent(UUID donationEventId, UserDetails userDetails) {
        DonationEvent event = donationEventRepository.findById(donationEventId)
                .orElseThrow(() -> new NotFoundException(
                        "Không tìm thấy donation event với id: " + donationEventId));

        if (event.getStatus() != EventStatus.UPCOMING) {
            throw new IllegalStateException(
                    "Chỉ có thể bắt đầu sự kiện đang UPCOMING, hiện tại: " + event.getStatus());
        }

        event.setStatus(EventStatus.ONGOING);
        donationEventRepository.save(event);

        return donationEventMapper.toResponseLog(event);
    }


    private void validateDistanceFilter(
            DonationEventFilterReq req) {

        boolean hasAnyDistanceField =
                req.getLatitude() != null
                        || req.getLongitude() != null
                        || req.getMaxDistanceKm() != null;

        if (hasAnyDistanceField) {

            if (req.getLatitude() == null
                    || req.getLongitude() == null
                    || req.getMaxDistanceKm() == null) {

                throw new BadRequestException(
                        "latitude, longitude và maxDistanceKm phải đi cùng nhau"
                );
            }
        }
    }

    private double calculateDistance(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        final int EARTH_RADIUS = 6371; // km

        double latDistance =
                Math.toRadians(lat2 - lat1);

        double lonDistance =
                Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(latDistance / 2)
                        * Math.sin(latDistance / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2)
                        * Math.sin(lonDistance / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return EARTH_RADIUS * c;
    }
}
