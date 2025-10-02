// consumer\src\main\java\com\ong\empuje\comunitario\consumer\repository\DonationRequestRepository.java

package com.ong.empuje.comunitario.consumer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ong.empuje.comunitario.consumer.model.DonationRequest;
import com.ong.empuje.comunitario.consumer.model.DonationRequestId;

@Repository
public interface DonationRequestRepository extends JpaRepository<DonationRequest, DonationRequestId> {
    Optional<DonationRequest> findByRequestIdAndOrganizationId(Integer requestId, Integer organizationId);
}