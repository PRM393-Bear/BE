package com.example.PRM.repository;

import com.example.PRM.entity.DonationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonationEventRepository extends JpaRepository<DonationEvent, UUID> {
    Optional<DonationEvent> findByTitle(String title);
    Optional<DonationEvent> findById(UUID donationEventId);
    List<DonationEvent> findByOrganizationDetail_Id(UUID organizationId);
}
