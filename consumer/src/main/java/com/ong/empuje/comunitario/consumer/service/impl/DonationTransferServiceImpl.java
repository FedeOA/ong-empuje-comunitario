package com.ong.empuje.comunitario.consumer.service.impl;

import com.ong.empuje.comunitario.consumer.model.DonationOffer;
import com.ong.empuje.comunitario.consumer.model.DonationTransfer;
import com.ong.empuje.comunitario.consumer.repository.DonationTransferRepository;
import com.ong.empuje.comunitario.consumer.service.DonationTransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DonationTransferServiceImpl implements DonationTransferService {

    private static final Logger logger = LoggerFactory.getLogger(DonationTransferServiceImpl.class);
    private final DonationTransferRepository donationTransferRepository;

    public DonationTransferServiceImpl(DonationTransferRepository donationTransferRepository) {
        this.donationTransferRepository = donationTransferRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DonationTransfer> save(DonationTransfer donationTransfer) {
        logger.debug("Saving User with id: {}", donationTransfer);
        try {
            return Optional.of(donationTransferRepository.save(donationTransfer));
        } catch (Exception e) {
            logger.error("Error finding User with id: {}", donationTransfer.getId(), e);
            throw new RuntimeException("Failed to find user: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DonationTransfer> findById(Integer id) {
        logger.debug("Finding User with id: {}", id);
        try {
            return donationTransferRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding User with id: {}", id, e);
            throw new RuntimeException("Failed to find user: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationTransfer> findAll() {
        logger.debug("Finding all Users");
        try {
            return donationTransferRepository.findAll();
        } catch (Exception e) {
            logger.error("Error finding all Users", e);
            throw new RuntimeException("Failed to find users: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DonationTransfer> findByTransferIdAndOrganizationId(int transferId, int organizationId) {
        logger.debug("Finding DonationTransfer with transferId: {} and organizationId: {}", transferId, organizationId);
        try {
            return donationTransferRepository.findByTransferIdAndOrganizationId(transferId, organizationId);
        } catch (Exception e) {
            logger.error("Error finding DonationTransfer with transferId: {} and organizationId: {}", transferId, organizationId, e);
            throw new RuntimeException("Failed to find donation transfer: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DonationTransfer> findByTransferId(int transferId) {
        logger.debug("Finding DonationTransfer with transferId: {}", transferId);
        try {
            return donationTransferRepository.findByTransferId(transferId);
        } catch (Exception e) {
            logger.error("Error finding DonationTransfer with transferId: {}", transferId, e);
            throw new RuntimeException("Failed to find donation transfer: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DonationTransfer> findById(int id) {
        logger.debug("Finding DonationTransfer with id: {}", id);
        try {
            return donationTransferRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding DonationTransfer with id: {}", id, e);
            throw new RuntimeException("Failed to find donation transfer: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DonationTransfer createDonationTransfer(DonationTransfer donationTransfer) {
        logger.debug("Creating DonationTransfer: {}", donationTransfer);
        try {
            validateDonationTransfer(donationTransfer);
            DonationTransfer savedTransfer = donationTransferRepository.save(donationTransfer);
            logger.info("Created DonationTransfer with transferId: {} and organizationId: {}", 
                savedTransfer.getTransferId(), savedTransfer.getOrganizationId());
            return savedTransfer;
        } catch (Exception e) {
            logger.error("Error creating DonationTransfer: {}", donationTransfer, e);
            throw new RuntimeException("Failed to create donation transfer: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DonationTransfer updateDonationTransfer(DonationTransfer donationTransfer) {
        logger.debug("Updating DonationTransfer: {}", donationTransfer);
        try {
            validateDonationTransfer(donationTransfer);
            Optional<DonationTransfer> existingTransfer = donationTransferRepository.findByTransferIdAndOrganizationId(
                donationTransfer.getTransferId(), donationTransfer.getOrganizationId());
            if (existingTransfer.isEmpty()) {
                logger.error("DonationTransfer not found for transferId: {} and organizationId: {}", 
                    donationTransfer.getTransferId(), donationTransfer.getOrganizationId());
                throw new RuntimeException("Donation transfer not found");
            }
            DonationTransfer updatedTransfer = donationTransferRepository.save(donationTransfer);
            logger.info("Updated DonationTransfer with transferId: {} and organizationId: {}", 
                updatedTransfer.getTransferId(), updatedTransfer.getOrganizationId());
            return updatedTransfer;
        } catch (Exception e) {
            logger.error("Error updating DonationTransfer: {}", donationTransfer, e);
            throw new RuntimeException("Failed to update donation transfer: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteDonationTransfer(int transferId, int organizationId) {
        logger.debug("Deleting DonationTransfer with transferId: {} and organizationId: {}", transferId, organizationId);
        try {
            Optional<DonationTransfer> transfer = donationTransferRepository.findByTransferIdAndOrganizationId(transferId, organizationId);
            if (transfer.isEmpty()) {
                logger.error("DonationTransfer not found for transferId: {} and organizationId: {}", transferId, organizationId);
                throw new RuntimeException("Donation transfer not found");
            }
            donationTransferRepository.delete(transfer.get());
            logger.info("Deleted DonationTransfer with transferId: {} and organizationId: {}", transferId, organizationId);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting DonationTransfer with transferId: {} and organizationId: {}", transferId, organizationId, e);
            throw new RuntimeException("Failed to delete donation transfer: " + e.getMessage());
        }
    }

    private void validateDonationTransfer(DonationTransfer donationTransfer) {
        if (donationTransfer.getTransferId() <= 0 || donationTransfer.getOrganizationId() <= 0) {
            throw new IllegalArgumentException("Transfer ID and Organization ID are required and must be positive numbers");
        }
    }
}