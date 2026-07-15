package com.example.PRM.repository;

import com.example.PRM.entity.OrganizationDetail;
import com.example.PRM.status_enum.VerificationOrganizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationDetailRepository extends JpaRepository<OrganizationDetail,UUID> {
    Optional<OrganizationDetail> findById(UUID organizationDetailId);
    Optional<OrganizationDetail> findByOrOrgName(String orgName);

    Optional<OrganizationDetail> findByUser_UserId(UUID userUserId);

    List<OrganizationDetail> findByStatus(VerificationOrganizationStatus status);
    @Query(value = "SELECT * FROM organization_detail o WHERE " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(o.latitude)) * " +
            "cos(radians(o.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(o.latitude)))) <= :radius " +
            "ORDER BY " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(o.latitude)) * " +
            "cos(radians(o.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(o.latitude)))) ASC", nativeQuery = true)
    List<OrganizationDetail> findNearbyOrganizations(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radius") double radius);
}
