package com.example.PRM.repository;

import com.example.PRM.entity.DonationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DonationRequestRepository extends JpaRepository<DonationRequest, Long> {
    DonationRequest findById(UUID donationRequestId);
}
