package com.ong.empuje.comunitario.consumer.service.impl;

import com.ong.empuje.comunitario.consumer.model.VoluntaryEvents;
import com.ong.empuje.comunitario.consumer.repository.VoluntaryEventsRepository;
import com.ong.empuje.comunitario.consumer.service.VoluntaryEventsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VoluntaryEventsServiceImpl implements VoluntaryEventsService {

    private static final Logger logger = LoggerFactory.getLogger(VoluntaryEventsServiceImpl.class);
    private final VoluntaryEventsRepository voluntaryEventsRepository;

    public VoluntaryEventsServiceImpl(VoluntaryEventsRepository voluntaryEventsRepository) {
        this.voluntaryEventsRepository = voluntaryEventsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VoluntaryEvents> findById(Integer id) {
        logger.debug("Finding VoluntaryEvents with id: {}", id);
        try {
            return voluntaryEventsRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding VoluntaryEvents with id: {}", id, e);
            throw new RuntimeException("Failed to find voluntary events: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoluntaryEvents> findAll() {
        logger.debug("Finding all VoluntaryEvents");
        try {
            return voluntaryEventsRepository.findAll();
        } catch (Exception e) {
            logger.error("Error finding all VoluntaryEvents", e);
            throw new RuntimeException("Failed to find voluntary events: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public VoluntaryEvents createVoluntaryEvents(VoluntaryEvents voluntaryEvents) {
        logger.debug("Creating VoluntaryEvents: {}", voluntaryEvents);
        try {
            validateVoluntaryEvents(voluntaryEvents);
            VoluntaryEvents savedVoluntaryEvents = voluntaryEventsRepository.save(voluntaryEvents);
            logger.info("Created VoluntaryEvents with id: {}", savedVoluntaryEvents.getId());
            return savedVoluntaryEvents;
        } catch (Exception e) {
            logger.error("Error creating VoluntaryEvents: {}", voluntaryEvents, e);
            throw new RuntimeException("Failed to create voluntary events: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public VoluntaryEvents updateVoluntaryEvents(VoluntaryEvents voluntaryEvents) {
        logger.debug("Updating VoluntaryEvents: {}", voluntaryEvents);
        try {
            validateVoluntaryEvents(voluntaryEvents);
            Optional<VoluntaryEvents> existingVoluntaryEvents = voluntaryEventsRepository.findById(voluntaryEvents.getId());
            if (existingVoluntaryEvents.isEmpty()) {
                logger.error("VoluntaryEvents not found for id: {}", voluntaryEvents.getId());
                throw new RuntimeException("VoluntaryEvents not found");
            }
            VoluntaryEvents updatedVoluntaryEvents = voluntaryEventsRepository.save(voluntaryEvents);
            logger.info("Updated VoluntaryEvents with id: {}", updatedVoluntaryEvents.getId());
            return updatedVoluntaryEvents;
        } catch (Exception e) {
            logger.error("Error updating VoluntaryEvents: {}", voluntaryEvents, e);
            throw new RuntimeException("Failed to update voluntary events: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteVoluntaryEvents(Integer id) {
        logger.debug("Deleting VoluntaryEvents with id: {}", id);
        try {
            Optional<VoluntaryEvents> voluntaryEvents = voluntaryEventsRepository.findById(id);
            if (voluntaryEvents.isEmpty()) {
                logger.error("VoluntaryEvents not found for id: {}", id);
                throw new RuntimeException("VoluntaryEvents not found");
            }
            voluntaryEventsRepository.deleteById(id);
            logger.info("Deleted VoluntaryEvents with id: {}", id);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting VoluntaryEvents with id: {}", id, e);
            throw new RuntimeException("Failed to delete voluntary events: " + e.getMessage());
        }
    }

    private void validateVoluntaryEvents(VoluntaryEvents voluntaryEvents) {
        if (voluntaryEvents.getId() == null) {
            throw new IllegalArgumentException("VoluntaryEvents ID is required");
        }
        if (voluntaryEvents.getVoluntary() == null || voluntaryEvents.getEvent() == null) {
            throw new IllegalArgumentException("Voluntary ID and Event ID are required");
        }
    }
}