package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.DonationRequestReq;
import com.example.PRM.dto.response.DonationPendingResponse;
import com.example.PRM.entity.*;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.DonationRequestMapper;
import com.example.PRM.repository.*;
import com.example.PRM.service.DonationRequestService;
import com.example.PRM.status_enum.DonationStatus;
import com.example.PRM.status_enum.WardrobeStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DonationRequestServiceImpl implements DonationRequestService {
    private final DonationRequestRepository donationRequestRepository;
    private final DonationEventRepository donationEventRepository;
    private final WardrobeItemRepository wardrobeItemRepository;
    private final OrganizationDetailRepository organizationDetailRepository;
    private final DonationRequestMapper donationRequestMapper;
    private final UserRepository userRepository;
    private final NotificationAdminServiceImpl notificationAdminService;

    public DonationRequestServiceImpl(DonationRequestRepository donationRequestRepository, DonationEventRepository donationEventRepository, WardrobeItemRepository wardrobeItemRepository, OrganizationDetailRepository organizationDetailRepository, DonationRequestMapper donationRequestMapper, UserRepository userRepository, NotificationAdminServiceImpl notificationAdminService) {
        this.donationRequestRepository = donationRequestRepository;
        this.donationEventRepository = donationEventRepository;
        this.wardrobeItemRepository = wardrobeItemRepository;
        this.organizationDetailRepository = organizationDetailRepository;
        this.donationRequestMapper = donationRequestMapper;
        this.userRepository = userRepository;
        this.notificationAdminService = notificationAdminService;
    }

    @Override
    public void createDonationRequest(DonationRequestReq donationRequestReq, UserDetails userDetails) {
        DonationRequest donationRequest = donationRequestMapper.toEntity(donationRequestReq);

        donationRequest.setStatus(DonationStatus.PENDING);

        DonationEvent de = donationEventRepository.findByTitle(donationRequestReq.getDonationEventName()).orElse(null);
        donationRequest.setDonationEvent(de);

        WardrobeItem wi = wardrobeItemRepository.findByName(donationRequestReq.getItemName()).orElseThrow(()
                -> new NotFoundException("Wardrobe item not found with name: " + donationRequestReq.getItemName()));

        wi.setStatus(WardrobeStatus.LISTED);

        donationRequest.getItems().add(wi);

        OrganizationDetail od = organizationDetailRepository.findByOrOrgName(donationRequestReq.getOrganizationName()).orElseThrow(()
                -> new NotFoundException("Organization detail not found with orgName: " + donationRequestReq.getDonationEventName()));
        donationRequest.setOrganizationDetail(od);

        User user = userRepository.findByUserName(userDetails.getUsername()).orElseThrow(()
                -> new NotFoundException("User not found with userName: " + userDetails.getUsername()));

        donationRequest.setUser(user);

        donationRequest.setCreatedAt(LocalDateTime.now());

        donationRequestRepository.save(donationRequest);

    }

    private void updateStatus(
            UUID donationRequestId,
            DonationStatus expectedStatus,
            DonationStatus newStatus) {

        DonationRequest donationRequest =
                donationRequestRepository.findById(donationRequestId)
                        .orElseThrow(() ->
                                new NotFoundException("Donation request not found"));

        if (donationRequest.getStatus() != expectedStatus) {
            throw new BadRequestException(
                    "Invalid status transition");
        }

        donationRequest.setStatus(newStatus);

        donationRequestRepository.save(donationRequest);
    }

    @Override
    public void accept(UUID donationRequestId) {
        updateStatus(
                donationRequestId,
                DonationStatus.PENDING,
                DonationStatus.ACCEPTED
        );
        DonationRequest donationRequest = donationRequestRepository.findById(donationRequestId).orElseThrow(() ->
                new NotFoundException("Donation request not found"));
        donationRequest.setAcceptedAt(LocalDateTime.now());
    }

    @Override
    public void shipping(UUID donationRequestId) {
        updateStatus(
                donationRequestId,
                DonationStatus.ACCEPTED,
                DonationStatus.SHIPPING
        );
        DonationRequest donationRequest = donationRequestRepository.findById(donationRequestId).orElseThrow(() ->
                new NotFoundException("Donation request not found"));
        donationRequest.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    public void shipped(UUID donationRequestId) {
        updateStatus(
                donationRequestId,
                DonationStatus.SHIPPING,
                DonationStatus.SHIPPED
        );
        DonationRequest donationRequest = donationRequestRepository.findById(donationRequestId).orElseThrow(() ->
                new NotFoundException("Donation request not found"));
        donationRequest.setShippedAt(LocalDateTime.now());
    }

    @Override
    public void received(UUID donationRequestId) {
        updateStatus(
                donationRequestId,
                DonationStatus.SHIPPED,
                DonationStatus.RECEIVED
        );
        DonationRequest donationRequest = donationRequestRepository.findById(donationRequestId).orElseThrow(() ->
                new NotFoundException("Donation request not found"));
        donationRequest.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    public void reject(UUID donationRequestId, String reason) {

        DonationRequest donationRequest =
                donationRequestRepository.findById(donationRequestId)
                        .orElseThrow(() ->
                                new NotFoundException("Donation request not found"));

        if (donationRequest.getStatus() != DonationStatus.PENDING) {
            throw new BadRequestException(
                    "Only pending donation can be rejected");
        }



        donationRequest.setStatus(DonationStatus.REJECTED);
        donationRequest.setRejectedReason(reason);
        donationRequest.setUpdatedAt(LocalDateTime.now());

        donationRequestRepository.save(donationRequest);
    }

    @Override
    public void cancel(UUID donationRequestId, String cancelReason) {

        DonationRequest donationRequest =
                donationRequestRepository.findById(donationRequestId)
                        .orElseThrow(() ->
                                new NotFoundException("Donation request not found"));

        donationRequest.setCancelReason(cancelReason);

        DonationStatus currentStatus = donationRequest.getStatus();

        if (currentStatus != DonationStatus.PENDING
                && currentStatus != DonationStatus.ACCEPTED) {

            throw new BadRequestException(
                    "Only pending or accepted donations can be cancelled");
        }

        donationRequest.setStatus(DonationStatus.CANCELLED);

        donationRequest.setUpdatedAt(LocalDateTime.now());

        donationRequestRepository.save(donationRequest);
    }

    @Override
    @Scheduled(cron = "0 0 8 * * *")
    public void checkPendingDonations(){
        LocalDateTime deadline = LocalDateTime.now().minusDays(5);

        System.out.println("=== SCHEDULE RUNNING ===");

        List<DonationRequest> donations =
                donationRequestRepository
                        .findPendingDonationsOverdue(
                                DonationStatus.PENDING,
                                deadline
                        );

        System.out.println("Found donations: " + donations.size());
        notificationAdminService.notifyAdminPendingOverdue(donations);
    }

    @Override
    public void assignOrganization(UUID donationRequestId, UUID organizationId) {

        DonationRequest donationRequest =
                donationRequestRepository.findById(donationRequestId)
                        .orElseThrow(() ->
                                new NotFoundException("Donation not found"));

        // CHECK BUSINESS RULE
        LocalDateTime deadline = LocalDateTime.now().minusDays(5);

        if (donationRequest.getStatus() != DonationStatus.PENDING
                || donationRequest.getCreatedAt().isAfter(deadline)) {

            throw new BadRequestException(
                    "Only PENDING donations older than 5 days can be reassigned"
            );
        }

        OrganizationDetail organization =
                organizationDetailRepository.findById(organizationId)
                        .orElseThrow(() ->
                                new NotFoundException("Organization not found"));

        donationRequest.setOrganizationDetail(organization);

        donationRequest.setUpdatedAt(LocalDateTime.now());

        donationRequestRepository.save(donationRequest);
    }

    @Override
    public List<DonationPendingResponse> getPendingDonations(UserDetails userDetails) {
        List<DonationRequest> lists = donationRequestRepository.findPendingDonationsOverdue(DonationStatus.PENDING
                ,LocalDateTime.now().minusDays(5));

        if(lists.isEmpty()){
            throw new NotFoundException("No pending donations found");
        }

        return lists.stream().map(donationRequestMapper::toPendingResponse).toList();
    }


    @Override
    public void completed(UUID donationRequestId) {
        updateStatus(
                donationRequestId,
                DonationStatus.RECEIVED,
                DonationStatus.COMPLETED
        );
        DonationRequest donationRequest = donationRequestRepository.findById(donationRequestId).orElseThrow(() ->
                new NotFoundException("Donation request not found"));
        donationRequest.setCompletedAt(LocalDateTime.now());

        List<WardrobeItem> items = donationRequest.getItems();
        for(WardrobeItem item : items){
            item.setStatus(WardrobeStatus.DONATED);
            wardrobeItemRepository.save(item);
        }
    }


}
