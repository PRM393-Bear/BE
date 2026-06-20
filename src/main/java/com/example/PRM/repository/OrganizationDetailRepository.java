package com.example.PRM.repository;

import com.example.PRM.entity.OrganizationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationDetailRepository extends JpaRepository<OrganizationDetail,Long> {
    Optional<OrganizationDetail> findById(UUID organizationDetailId);

    Optional<OrganizationDetail> findByOrOrgName(String orgName);
}
