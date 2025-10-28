package com.ong.empuje.comunitario.consumer.service.impl;

import com.ong.empuje.comunitario.consumer.model.DonationOffer;
import com.ong.empuje.comunitario.consumer.model.Organization;
import com.ong.empuje.comunitario.consumer.repository.OrganizationRepository;
import com.ong.empuje.comunitario.consumer.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationServiceImpl.class);
    private final OrganizationRepository organizationRepository;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> findById(Integer id) {
        logger.debug("Finding Organization with id: {}", id);
        try {
            return organizationRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding Organization with id: {}", id, e);
            throw new RuntimeException("Failed to find organization: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organization> findAll() {
        logger.debug("Finding all Organizations");
        try {
            return organizationRepository.findAll();
        } catch (Exception e) {
            logger.error("Error finding all Organizations", e);
            throw new RuntimeException("Failed to find organizations: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Organization createOrganization(Organization organization) {
        logger.debug("Creating Organization: {}", organization);
        try {
            validateOrganization(organization);
            Organization savedOrganization = organizationRepository.save(organization);
            logger.info("Created Organization with id: {}", savedOrganization.getId());
            return savedOrganization;
        } catch (Exception e) {
            logger.error("Error creating Organization: {}", organization, e);
            throw new RuntimeException("Failed to create organization: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Organization updateOrganization(Organization organization) {
        logger.debug("Updating Organization: {}", organization);
        try {
            validateOrganization(organization);
            Optional<Organization> existingOrganization = organizationRepository.findById(organization.getId());
            if (existingOrganization.isEmpty()) {
                logger.error("Organization not found for id: {}", organization.getId());
                throw new RuntimeException("Organization not found");
            }
            Organization updatedOrganization = organizationRepository.save(organization);
            logger.info("Updated Organization with id: {}", updatedOrganization.getId());
            return updatedOrganization;
        } catch (Exception e) {
            logger.error("Error updating Organization: {}", organization, e);
            throw new RuntimeException("Failed to update organization: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteOrganization(Integer id) {
        logger.debug("Deleting Organization with id: {}", id);
        try {
            Optional<Organization> organization = organizationRepository.findById(id);
            if (organization.isEmpty()) {
                logger.error("Organization not found for id: {}", id);
                throw new RuntimeException("Organization not found");
            }
            organizationRepository.deleteById(id);
            logger.info("Deleted Organization with id: {}", id);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting Organization with id: {}", id, e);
            throw new RuntimeException("Failed to delete organization: " + e.getMessage());
        }
    }

    private void validateOrganization(Organization organization) {
        if (organization.getId() == null) {
            throw new IllegalArgumentException("Organization ID is required");
        }
        if (organization.getName() == null || organization.getName().isEmpty()) {
            throw new IllegalArgumentException("Organization name is required");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> save(Organization organization) {
        logger.debug("Saving User with id: {}", organization);
        try {
            return Optional.of(organizationRepository.save(organization));
        } catch (Exception e) {
            logger.error("Error finding User with id: {}", organization.getId(), e);
            throw new RuntimeException("Failed to find user: " + e.getMessage());
        }
    }
}