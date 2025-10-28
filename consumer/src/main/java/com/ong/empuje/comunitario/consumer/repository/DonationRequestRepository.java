package com.ong.empuje.comunitario.consumer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ong.empuje.comunitario.consumer.model.DonationRequest;
import com.ong.empuje.comunitario.consumer.model.DonationRequestId;

@Repository
public interface DonationRequestRepository extends JpaRepository<DonationRequest, DonationRequestId> {
    Optional<DonationRequest> findByRequestIdAndOrganizationId(Integer requestId, Integer organizationId);

    @Modifying
    @Transactional
    @Query("UPDATE DonationRequest dr SET dr.deleted = true WHERE dr.requestId = ?1 AND dr.organizationId = ?2")
    int setDeletedByRequestIdAndOrganizationId(Integer requestId, Integer organizationId);

    @Query("SELECT COUNT(dr) > 0 FROM DonationRequest dr WHERE dr.requestId = :requestId AND dr.organizationId = :organizationId")
    boolean existsByRequestIdAndOrganizationId(Integer requestId, Integer organizationId);
}