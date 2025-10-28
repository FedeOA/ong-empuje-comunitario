package com.ong.empuje.comunitario.consumer.service.impl;

import com.ong.empuje.comunitario.consumer.model.DonationOffer;
import com.ong.empuje.comunitario.consumer.model.DonationRequest;
import com.ong.empuje.comunitario.consumer.repository.DonationRequestRepository;
import com.ong.empuje.comunitario.consumer.service.DonationRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DonationRequestServiceImpl implements DonationRequestService {

    private static final Logger logger = LoggerFactory.getLogger(DonationRequestServiceImpl.class);
    private final DonationRequestRepository donationRequestRepository;

    public DonationRequestServiceImpl(DonationRequestRepository donationRequestRepository) {
        this.donationRequestRepository = donationRequestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DonationRequest> save(DonationRequest donationRequest) {
        logger.debug("Saving User with id: {}", donationRequest);
        try {
            return Optional.of(donationRequestRepository.save(donationRequest));
        } catch (Exception e) {
            logger.error("Error finding User with id: {}", donationRequest.getRequestId(), e);
            throw new RuntimeException("Failed to find user: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationRequest> findAll() {
        logger.debug("Finding all Users");
        try {
            return donationRequestRepository.findAll();
        } catch (Exception e) {
            logger.error("Error finding all Users", e);
            throw new RuntimeException("Failed to find users: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DonationRequest> findByRequestIdAndOrganizationId(Integer requestId, Integer organizationId) {
        logger.debug("Finding DonationRequest with requestId: {} and organizationId: {}", requestId, organizationId);
        try {
            return donationRequestRepository.findByRequestIdAndOrganizationId(requestId, organizationId);
        } catch (Exception e) {
            logger.error("Error finding DonationRequest with requestId: {} and organizationId: {}", requestId, organizationId, e);
            throw new RuntimeException("Failed to find donation request: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DonationRequest createDonationRequest(DonationRequest donationRequest) {
        logger.debug("Creating DonationRequest: {}", donationRequest);
        try {
            validateDonationRequest(donationRequest);
            DonationRequest savedRequest = donationRequestRepository.save(donationRequest);
            logger.info("Created DonationRequest with requestId: {} and organizationId: {}", 
                savedRequest.getRequestId(), savedRequest.getOrganizationId());
            return savedRequest;
        } catch (Exception e) {
            logger.error("Error creating DonationRequest: {}", donationRequest, e);
            throw new RuntimeException("Failed to create donation request: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteDonationRequest(Integer requestId, Integer organizationId) {
        logger.debug("Deleting DonationRequest with requestId: {} and organizationId: {}", requestId, organizationId);
        try {
            int updatedRows = donationRequestRepository.setDeletedByRequestIdAndOrganizationId(requestId, organizationId);
            if (updatedRows == 0) {
                logger.error("DonationRequest not found for requestId: {} and organizationId: {}", requestId, organizationId);
                throw new RuntimeException("Donation request not found");
            }
            logger.info("Marked DonationRequest as deleted with requestId: {} and organizationId: {}", requestId, organizationId);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting DonationRequest with requestId: {} and organizationId: {}", requestId, organizationId, e);
            throw new RuntimeException("Failed to delete donation request: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRequestIdAndOrganizationId(Integer requestId, Integer organizationId) {
        logger.debug("Checking existence of DonationRequest with requestId: {} and organizationId: {}", requestId, organizationId);
        try {
            return donationRequestRepository.existsByRequestIdAndOrganizationId(requestId, organizationId);
        } catch (Exception e) {
            logger.error("Error checking existence of DonationRequest with requestId: {} and organizationId: {}", 
                requestId, organizationId, e);
            throw new RuntimeException("Failed to check donation request existence: " + e.getMessage());
        }
    }

    private void validateDonationRequest(DonationRequest donationRequest) {
        if (donationRequest.getRequestId() == null || donationRequest.getOrganizationId() == null) {
            throw new IllegalArgumentException("Request ID and Organization ID are required");
        }
        if (donationRequest.getDeleted() == null) {
            donationRequest.setDeleted(false); 
        }
    }
}