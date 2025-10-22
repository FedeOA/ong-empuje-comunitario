package com.ong.empuje.comunitario.consumer.service.impl;

import com.ong.empuje.comunitario.consumer.model.Voluntary;
import com.ong.empuje.comunitario.consumer.repository.VoluntaryRepository;
import com.ong.empuje.comunitario.consumer.service.VoluntaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VoluntaryServiceImpl implements VoluntaryService {

    private static final Logger logger = LoggerFactory.getLogger(VoluntaryServiceImpl.class);
    private final VoluntaryRepository voluntaryRepository;

    public VoluntaryServiceImpl(VoluntaryRepository voluntaryRepository) {
        this.voluntaryRepository = voluntaryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Voluntary> findById(Integer id) {
        logger.debug("Finding Voluntary with id: {}", id);
        try {
            return voluntaryRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding Voluntary with id: {}", id, e);
            throw new RuntimeException("Failed to find voluntary: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Voluntary> findByOrganizationIdAndVoluntaryId(Integer organizationId, Integer voluntaryId) {
        logger.debug("Finding Voluntary with organizationId: {} and voluntaryId: {}", organizationId, voluntaryId);
        try {
            return voluntaryRepository.findByOrganizationIdAndVoluntaryId(organizationId, voluntaryId);
        } catch (Exception e) {
            logger.error("Error finding Voluntary with organizationId: {} and voluntaryId: {}", organizationId, voluntaryId, e);
            throw new RuntimeException("Failed to find voluntary: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Voluntary> findAll() {
        logger.debug("Finding all Voluntaries");
        try {
            return voluntaryRepository.findAll();
        } catch (Exception e) {
            logger.error("Error finding all Voluntaries", e);
            throw new RuntimeException("Failed to find voluntaries: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Voluntary createVoluntary(Voluntary voluntary) {
        logger.debug("Creating Voluntary: {}", voluntary);
        try {
            validateVoluntary(voluntary);
            Voluntary savedVoluntary = voluntaryRepository.save(voluntary);
            logger.info("Created Voluntary with id: {} and voluntaryId: {}", savedVoluntary.getId(), savedVoluntary.getVoluntaryId());
            return savedVoluntary;
        } catch (Exception e) {
            logger.error("Error creating Voluntary: {}", voluntary, e);
            throw new RuntimeException("Failed to create voluntary: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Voluntary updateVoluntary(Voluntary voluntary) {
        logger.debug("Updating Voluntary: {}", voluntary);
        try {
            validateVoluntary(voluntary);
            Optional<Voluntary> existingVoluntary = voluntaryRepository.findById(voluntary.getId());
            if (existingVoluntary.isEmpty()) {
                logger.error("Voluntary not found for id: {}", voluntary.getId());
                throw new RuntimeException("Voluntary not found");
            }
            Voluntary updatedVoluntary = voluntaryRepository.save(voluntary);
            logger.info("Updated Voluntary with id: {} and voluntaryId: {}", updatedVoluntary.getId(), updatedVoluntary.getVoluntaryId());
            return updatedVoluntary;
        } catch (Exception e) {
            logger.error("Error updating Voluntary: {}", voluntary, e);
            throw new RuntimeException("Failed to update voluntary: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteVoluntary(Integer id) {
        logger.debug("Deleting Voluntary with id: {}", id);
        try {
            Optional<Voluntary> voluntary = voluntaryRepository.findById(id);
            if (voluntary.isEmpty()) {
                logger.error("Voluntary not found for id: {}", id);
                throw new RuntimeException("Voluntary not found");
            }
            voluntaryRepository.deleteById(id);
            logger.info("Deleted Voluntary with id: {}", id);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting Voluntary with id: {}", id, e);
            throw new RuntimeException("Failed to delete voluntary: " + e.getMessage());
        }
    }

    private void validateVoluntary(Voluntary voluntary) {
        if (voluntary.getId() == null) {
            throw new IllegalArgumentException("Voluntary ID is required");
        }
        if (voluntary.getVoluntaryId() == null) {
            throw new IllegalArgumentException("VoluntaryId is required");
        }
        if (voluntary.getOrganizationId() == null) {
            throw new IllegalArgumentException("Organization ID is required");
        }
        // Add additional validation as needed, e.g., checking for required fields like name
        if (voluntary.getName() == null || voluntary.getName().isEmpty()) {
            throw new IllegalArgumentException("Voluntary name is required");
        }
    }
}