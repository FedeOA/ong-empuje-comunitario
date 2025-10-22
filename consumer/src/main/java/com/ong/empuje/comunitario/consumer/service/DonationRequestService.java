package com.ong.empuje.comunitario.consumer.service;

import com.ong.empuje.comunitario.consumer.model.DonationRequest;

import java.util.List;
import java.util.Optional;

public interface DonationRequestService {

    Optional<DonationRequest> save(DonationRequest donationRequest);

    List<DonationRequest> findAll();

    Optional<DonationRequest> findByRequestIdAndOrganizationId(Integer requestId, Integer organizationId);

    DonationRequest createDonationRequest(DonationRequest donationRequest);

    boolean deleteDonationRequest(Integer requestId, Integer organizationId);

    boolean existsByRequestIdAndOrganizationId(Integer requestId, Integer organizationId);
}