package com.ong.empuje.comunitario.consumer.service.impl;

import com.ong.empuje.comunitario.consumer.model.DonationOffer;
import com.ong.empuje.comunitario.consumer.repository.DonationOfferRepository;
import com.ong.empuje.comunitario.consumer.service.DonationOfferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DonationOfferServiceImpl implements DonationOfferService {

    private static final Logger logger = LoggerFactory.getLogger(DonationOfferServiceImpl.class);
    private final DonationOfferRepository donationOfferRepository;

    public DonationOfferServiceImpl(DonationOfferRepository donationOfferRepository) {
        this.donationOfferRepository = donationOfferRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DonationOffer> save(DonationOffer donationOffer) {
        logger.debug("Saving User with id: {}", donationOffer);
        try {
            return Optional.of(donationOfferRepository.save(donationOffer));
        } catch (Exception e) {
            logger.error("Error finding User with id: {}", donationOffer.getId(), e);
            throw new RuntimeException("Failed to find user: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DonationOffer> findById(Integer id) {
        logger.debug("Finding User with id: {}", id);
        try {
            return donationOfferRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding User with id: {}", id, e);
            throw new RuntimeException("Failed to find user: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationOffer> findAll() {
        logger.debug("Finding all Users");
        try {
            return donationOfferRepository.findAll();
        } catch (Exception e) {
            logger.error("Error finding all Users", e);
            throw new RuntimeException("Failed to find users: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DonationOffer> findByOfferIdAndOrganizationId(int offerId, int organizationId) {
        logger.debug("Finding DonationOffer with offerId: {} and organizationId: {}", offerId, organizationId);
        try {
            return donationOfferRepository.findByOfferIdAndOrganizationId(offerId, organizationId);
        } catch (Exception e) {
            logger.error("Error finding DonationOffer with offerId: {} and organizationId: {}", offerId, organizationId, e);
            throw new RuntimeException("Failed to find donation offer: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DonationOffer> findByOfferId(int offerId) {
        logger.debug("Finding DonationOffer with offerId: {}", offerId);
        try {
            return donationOfferRepository.findByOfferId(offerId);
        } catch (Exception e) {
            logger.error("Error finding DonationOffer with offerId: {}", offerId, e);
            throw new RuntimeException("Failed to find donation offer: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationOffer> findByAvailableTrueAndExpiresAtAfter(LocalDateTime date) {
        logger.debug("Finding available DonationOffers expiring after: {}", date);
        try {
            return donationOfferRepository.findByAvailableTrueAndExpiresAtAfter(date);
        } catch (Exception e) {
            logger.error("Error finding available DonationOffers expiring after: {}", date, e);
            throw new RuntimeException("Failed to find available donation offers: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DonationOffer createDonationOffer(DonationOffer donationOffer) {
        logger.debug("Creating DonationOffer: {}", donationOffer);
        try {
            validateDonationOffer(donationOffer);
            DonationOffer savedOffer = donationOfferRepository.save(donationOffer);
            logger.info("Created DonationOffer with offerId: {}", savedOffer.getOfferId());
            return savedOffer;
        } catch (Exception e) {
            logger.error("Error creating DonationOffer: {}", donationOffer, e);
            throw new RuntimeException("Failed to create donation offer: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DonationOffer updateDonationOffer(DonationOffer donationOffer) {
        logger.debug("Updating DonationOffer: {}", donationOffer);
        try {
            validateDonationOffer(donationOffer);
            Optional<DonationOffer> existingOffer = donationOfferRepository.findByOfferIdAndOrganizationId(
                donationOffer.getOfferId(), donationOffer.getOrganizationId());
            if (existingOffer.isEmpty()) {
                logger.error("DonationOffer not found for offerId: {} and organizationId: {}", 
                    donationOffer.getOfferId(), donationOffer.getOrganizationId());
                throw new RuntimeException("Donation offer not found");
            }
            DonationOffer updatedOffer = donationOfferRepository.save(donationOffer);
            logger.info("Updated DonationOffer with offerId: {}", updatedOffer.getOfferId());
            return updatedOffer;
        } catch (Exception e) {
            logger.error("Error updating DonationOffer: {}", donationOffer, e);
            throw new RuntimeException("Failed to update donation offer: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteDonationOffer(int offerId, int organizationId) {
        logger.debug("Deleting DonationOffer with offerId: {} and organizationId: {}", offerId, organizationId);
        try {
            Optional<DonationOffer> offer = donationOfferRepository.findByOfferIdAndOrganizationId(offerId, organizationId);
            if (offer.isEmpty()) {
                logger.error("DonationOffer not found for offerId: {} and organizationId: {}", offerId, organizationId);
                throw new RuntimeException("Donation offer not found");
            }
            donationOfferRepository.delete(offer.get());
            logger.info("Deleted DonationOffer with offerId: {}", offerId);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting DonationOffer with offerId: {} and organizationId: {}", offerId, organizationId, e);
            throw new RuntimeException("Failed to delete donation offer: " + e.getMessage());
        }
    }

    private void validateDonationOffer(DonationOffer donationOffer) {
        if (donationOffer.getOfferId() <= 0 || donationOffer.getOrganizationId() <= 0) {
            throw new IllegalArgumentException("Offer ID and Organization ID must be positive numbers");
        }
        if (donationOffer.getExpiresAt() != null && donationOffer.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expiration date cannot be in the past");
        }
    }
}