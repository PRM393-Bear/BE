package com.example.PRM.repository;

import com.example.PRM.entity.DonationRequest;
import com.example.PRM.status_enum.DonationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonationRequestRepository extends JpaRepository<DonationRequest, Long> {
    Optional<DonationRequest> findById(UUID donationRequestId);

    @Query("""
    SELECT d
    FROM DonationRequest d
    JOIN FETCH d.user
    JOIN FETCH d.organizationDetail
    WHERE d.status = :status
      AND d.createdAt < :deadline
""")
    List<DonationRequest> findPendingDonationsOverdue(
            DonationStatus status,
            LocalDateTime deadline
    );
    List<DonationRequest> findByOrganizationDetail_Id(UUID organizationId);
    List<DonationRequest> findByUser_UserId(UUID userId);

}
