package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.DonationRequestCustomReq;
import com.example.PRM.dto.request.DonationRequestReq;
import com.example.PRM.dto.request.ReceivedReq;
import com.example.PRM.dto.request.ShippingReq;
import com.example.PRM.dto.response.DonationPendingResponse;
import com.example.PRM.dto.response.UploadRes;
import com.example.PRM.entity.*;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.DonationRequestMapper;
import com.example.PRM.mapper.WardrobeItemMapper;
import com.example.PRM.repository.*;
import com.example.PRM.service.DonationRequestService;
import com.example.PRM.status_enum.DonationStatus;
import com.example.PRM.status_enum.WardrobeStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DonationRequestServiceImpl implements DonationRequestService {
    private final DonationRequestRepository donationRequestRepository;
    private final DonationEventRepository donationEventRepository;
    private final WardrobeItemRepository wardrobeItemRepository;
    private final OrganizationDetailRepository organizationDetailRepository;
    private final DonationRequestMapper donationRequestMapper;
    private final UserRepository userRepository;
    private final NotificationAdminServiceImpl notificationAdminService;
    private final UploadServiceImpl uploadService;
    private final WardrobeItemMapper wardrobeItemMapper;

    public DonationRequestServiceImpl(DonationRequestRepository donationRequestRepository, DonationEventRepository donationEventRepository, WardrobeItemRepository wardrobeItemRepository, OrganizationDetailRepository organizationDetailRepository, DonationRequestMapper donationRequestMapper, UserRepository userRepository, NotificationAdminServiceImpl notificationAdminService, UploadServiceImpl uploadService, WardrobeItemMapper wardrobeItemMapper) {
        this.donationRequestRepository = donationRequestRepository;
        this.donationEventRepository = donationEventRepository;
        this.wardrobeItemRepository = wardrobeItemRepository;
        this.organizationDetailRepository = organizationDetailRepository;
        this.donationRequestMapper = donationRequestMapper;
        this.userRepository = userRepository;
        this.notificationAdminService = notificationAdminService;
        this.uploadService = uploadService;
        this.wardrobeItemMapper = wardrobeItemMapper;
    }

    @Override
    public void createDonationRequest(DonationRequestReq donationRequestReq, UserDetails userDetails) {

        DonationRequest donationRequest = donationRequestMapper.toEntity(donationRequestReq);
        donationRequest.setStatus(DonationStatus.PENDING);

        DonationEvent de = donationEventRepository.findById(donationRequestReq.getDonationEventId())
                .orElseThrow(() -> new NotFoundException(
                        "Donation event not found with id: " + donationRequestReq.getDonationEventId()));
        donationRequest.setDonationEvent(de);

        WardrobeItem wi = wardrobeItemRepository.findById(donationRequestReq.getWardrobeItemId())
                .orElseThrow(() -> new NotFoundException(
                        "Wardrobe item not found with id: " + donationRequestReq.getWardrobeItemId()));

        if (wi.getStatus() != WardrobeStatus.OWNED) {
            throw new BadRequestException("Wardrobe item is not owned by the user");
        }
        if (!wi.getUser().getUserName().equals(userDetails.getUsername())) {
            throw new BadRequestException("User is not the owner of the wardrobe item");
        }

        donationRequest.setUser(wi.getUser());
        wi.setStatus(WardrobeStatus.LISTED);
        donationRequest.getItems().add(wi);
        donationRequest.setOrganizationDetail(de.getOrganizationDetail());
        donationRequest.setCreatedAt(LocalDateTime.now());

        donationRequestRepository.save(donationRequest);
    }

    @Override
    public void createDonationRequest(DonationRequestCustomReq donationRequestReq, UserDetails userDetails) {

        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException(
                        "User not found with userName: " + userDetails.getUsername()));

        UploadRes uploadRes = uploadService.uploadImage(
                donationRequestReq.getImage(),
                userDetails.getUsername()
        );

        WardrobeItem wi = wardrobeItemMapper.toEntity(donationRequestReq);
        wi.setImageUrl(uploadRes.getUrl());
        wi.setUser(user);
        wi.setStatus(WardrobeStatus.LISTED);
        wardrobeItemRepository.save(wi);

        DonationEvent donationEvent = donationEventRepository
                .findById(donationRequestReq.getDonationEventId())
                .orElseThrow(() -> new NotFoundException("Donation event not found"));

        DonationRequest donationRequest = donationRequestMapper.toEntity(donationRequestReq);
        donationRequest.setDonationEvent(donationEvent);
        donationRequest.setOrganizationDetail(donationEvent.getOrganizationDetail());
        donationRequest.setUser(user);
        donationRequest.setStatus(DonationStatus.PENDING);
        donationRequest.setCreatedAt(LocalDateTime.now());
        donationRequest.getImages().add(uploadRes.getUrl());
        donationRequest.getItems().add(wi);

        donationRequestRepository.save(donationRequest);
    }

    @Override
    public void accept(UUID donationRequestId, UserDetails userDetails) {

        DonationRequest donationRequest = donationRequestRepository.findById(donationRequestId)
                .orElseThrow(() -> new NotFoundException("Donation request not found"));

        if (!donationRequest.getOrganizationDetail().getUser().getUserName().equals(userDetails.getUsername())) {
            throw new BadRequestException("User cannot accept donation request because user not come from this organization");
        }

        if (donationRequest.getStatus() != DonationStatus.PENDING) {
            throw new BadRequestException("Only pending donation can be accepted");
        }

        donationRequest.setStatus(DonationStatus.ACCEPTED);
        donationRequest.setAcceptedAt(LocalDateTime.now());
        donationRequestRepository.save(donationRequest);
    }

    @Override
    public void reject(UUID donationRequestId, String reason, UserDetails userDetails) {

        DonationRequest donationRequest = donationRequestRepository.findById(donationRequestId)
                .orElseThrow(() -> new NotFoundException("Donation request not found"));

        if (!donationRequest.getOrganizationDetail().getUser().getUserName().equals(userDetails.getUsername())) {
            throw new BadRequestException("User cannot reject donation request because user not come from this organization");
        }

        if (donationRequest.getStatus() != DonationStatus.PENDING) {
            throw new BadRequestException("Only pending donation can be rejected");
        }

        donationRequest.setStatus(DonationStatus.REJECTED);
        donationRequest.setRejectedReason(reason);
        donationRequest.setUpdatedAt(LocalDateTime.now());
        donationRequestRepository.save(donationRequest);
    }

    @Override
    public void shipping(UUID donationRequestId, UserDetails userDetails) {

        DonationRequest donationRequest = donationRequestRepository.findById(donationRequestId)
                .orElseThrow(() -> new NotFoundException("Donation request not found"));

        if (!donationRequest.getUser().getUserName().equals(userDetails.getUsername())) {
            throw new BadRequestException("User is not the owner of the donation request");
        }

        if (donationRequest.getStatus() != DonationStatus.ACCEPTED) {
            throw new BadRequestException("Only accepted donation can be shipped");
        }

        donationRequest.setStatus(DonationStatus.SHIPPING);
        donationRequest.setUpdatedAt(LocalDateTime.now());
        donationRequestRepository.save(donationRequest);
    }

    @Override
    public void shipped(UUID donationRequestId, ShippingReq req, UserDetails userDetails) {

        DonationRequest donationRequest = donationRequestRepository.findById(donationRequestId)
                .orElseThrow(() -> new NotFoundException("Donation request not found"));

        if (!donationRequest.getUser().getUserName().equals(userDetails.getUsername())) {
            throw new BadRequestException("User is not the owner of the donation request");
        }

        if (donationRequest.getStatus() != DonationStatus.SHIPPING) {
            throw new BadRequestException("Only shipping donation can be marked as shipped");
        }

        UploadRes uploadRes = uploadService.uploadImage(req.getShippingProofFile(), userDetails.getUsername());

        donationRequest.setStatus(DonationStatus.SHIPPED);
        donationRequest.setTrackingCode(req.getTrackingCode());
        donationRequest.setShippingProofUrl(uploadRes.getUrl());
        donationRequest.setShippedAt(LocalDateTime.now());
        donationRequestRepository.save(donationRequest);
    }

    @Override
    public void received(UUID donationRequestId, ReceivedReq req, UserDetails userDetails) {

        DonationRequest donationRequest = donationRequestRepository.findById(donationRequestId)
                .orElseThrow(() -> new NotFoundException("Donation request not found"));

        if (!donationRequest.getOrganizationDetail().getUser().getUserName().equals(userDetails.getUsername())) {
            throw new BadRequestException("User is not the owner of the donation request");
        }

        if (donationRequest.getStatus() != DonationStatus.SHIPPED) {
            throw new BadRequestException("Only shipped donation can be marked as received");
        }

        UploadRes uploadRes = uploadService.uploadImage(req.getReceiptProofFile(), userDetails.getUsername());

        donationRequest.setStatus(DonationStatus.RECEIVED);
        donationRequest.setReceiptProofUrl(uploadRes.getUrl());
        donationRequest.setUpdatedAt(LocalDateTime.now());
        donationRequestRepository.save(donationRequest);
    }

    @Override
    public void completed(UUID donationRequestId) {

        DonationRequest donationRequest = donationRequestRepository.findById(donationRequestId)
                .orElseThrow(() -> new NotFoundException("Donation request not found"));

        switch (donationRequest.getStatus()) {
            case RECEIVED -> {
                donationRequest.setStatus(DonationStatus.COMPLETED);
                donationRequest.setCompletedAt(LocalDateTime.now());
                donationRequestRepository.save(donationRequest);
            }
            case SHIPPED -> {
                if (donationRequest.getShippedAt() == null ||
                        donationRequest.getShippedAt().plusDays(10).isAfter(LocalDateTime.now())) {
                    throw new BadRequestException(
                            "Cannot force complete: 10 days have not passed since SHIPPED");
                }
                donationRequest.setStatus(DonationStatus.COMPLETED);
                donationRequest.setCompletedAt(LocalDateTime.now());
                donationRequestRepository.save(donationRequest);
            }
            default -> throw new BadRequestException(
                    "Only RECEIVED or SHIPPED donations can be completed");
        }

        List<WardrobeItem> items = donationRequest.getItems();
        for (WardrobeItem item : items) {
            item.setStatus(WardrobeStatus.DONATED);
            wardrobeItemRepository.save(item);
        }
    }

    @Override
    public void cancel(UUID donationRequestId, String cancelReason, UserDetails userDetails) {

        DonationRequest donationRequest = donationRequestRepository.findById(donationRequestId)
                .orElseThrow(() -> new NotFoundException("Donation request not found"));

        if (!donationRequest.getUser().getUserName().equals(userDetails.getUsername())) {
            throw new BadRequestException("User is not the owner of the donation request");
        }

        DonationStatus currentStatus = donationRequest.getStatus();
        if (currentStatus != DonationStatus.PENDING && currentStatus != DonationStatus.ACCEPTED) {
            throw new BadRequestException("Only pending or accepted donations can be cancelled");
        }

        donationRequest.setCancelReason(cancelReason);
        donationRequest.setStatus(DonationStatus.CANCELLED);
        donationRequest.setCanceledAt(LocalDateTime.now());
        donationRequestRepository.save(donationRequest);
    }

    @Override
    @Scheduled(cron = "0 0 8 * * *")
    public void checkPendingDonations() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(5);

        System.out.println("=== SCHEDULE RUNNING ===");

        List<DonationRequest> donations = donationRequestRepository
                .findPendingDonationsOverdue(DonationStatus.PENDING, deadline);

        System.out.println("Found donations: " + donations.size());
        notificationAdminService.notifyAdminPendingOverdue(donations);
    }

    @Override
    public void assignOrganization(UUID donationRequestId, UUID organizationId) {

        DonationRequest donationRequest = donationRequestRepository.findById(donationRequestId)
                .orElseThrow(() -> new NotFoundException("Donation not found"));

        LocalDateTime deadline = LocalDateTime.now().minusDays(5);

        if (donationRequest.getStatus() != DonationStatus.PENDING
                || donationRequest.getCreatedAt().isAfter(deadline)) {
            throw new BadRequestException(
                    "Only PENDING donations older than 5 days can be reassigned");
        }

        OrganizationDetail organization = organizationDetailRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        donationRequest.setOrganizationDetail(organization);
        donationRequest.setUpdatedAt(LocalDateTime.now());
        donationRequestRepository.save(donationRequest);
    }

    @Override
    public List<DonationPendingResponse> getPendingDonations(UserDetails userDetails) {

        List<DonationRequest> lists = donationRequestRepository
                .findPendingDonationsOverdue(DonationStatus.PENDING, LocalDateTime.now().minusDays(5));

        if (lists.isEmpty()) {
            throw new NotFoundException("No pending donations found");
        }

        return lists.stream().map(donationRequestMapper::toPendingResponse).toList();
    }
}