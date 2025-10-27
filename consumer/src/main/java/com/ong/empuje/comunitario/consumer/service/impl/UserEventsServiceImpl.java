package com.ong.empuje.comunitario.consumer.service.impl;

import com.ong.empuje.comunitario.consumer.model.UserEvents;
import com.ong.empuje.comunitario.consumer.repository.UserEventsRepository;
import com.ong.empuje.comunitario.consumer.service.UserEventsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserEventsServiceImpl implements UserEventsService {

    private static final Logger logger = LoggerFactory.getLogger(UserEventsServiceImpl.class);
    private final UserEventsRepository userEventsRepository;

    public UserEventsServiceImpl(UserEventsRepository userEventsRepository) {
        this.userEventsRepository = userEventsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserEvents> findById(Integer id) {
        logger.debug("Finding UserEvents with id: {}", id);
        try {
            return userEventsRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding UserEvents with id: {}", id, e);
            throw new RuntimeException("Failed to find user events: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserEvents> findAll() {
        logger.debug("Finding all UserEvents");
        try {
            return userEventsRepository.findAll();
        } catch (Exception e) {
            logger.error("Error finding all UserEvents", e);
            throw new RuntimeException("Failed to find user events: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public UserEvents createUserEvents(UserEvents userEvents) {
        logger.debug("Creating UserEvents: {}", userEvents);
        try {
            validateUserEvents(userEvents);
            UserEvents savedUserEvents = userEventsRepository.save(userEvents);
            logger.info("Created UserEvents with id: {}", savedUserEvents.getId());
            return savedUserEvents;
        } catch (Exception e) {
            logger.error("Error creating UserEvents: {}", userEvents, e);
            throw new RuntimeException("Failed to create user events: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public UserEvents updateUserEvents(UserEvents userEvents) {
        logger.debug("Updating UserEvents: {}", userEvents);
        try {
            validateUserEvents(userEvents);
            Optional<UserEvents> existingUserEvents = userEventsRepository.findById(userEvents.getId());
            if (existingUserEvents.isEmpty()) {
                logger.error("UserEvents not found for id: {}", userEvents.getId());
                throw new RuntimeException("UserEvents not found");
            }
            UserEvents updatedUserEvents = userEventsRepository.save(userEvents);
            logger.info("Updated UserEvents with id: {}", updatedUserEvents.getId());
            return updatedUserEvents;
        } catch (Exception e) {
            logger.error("Error updating UserEvents: {}", userEvents, e);
            throw new RuntimeException("Failed to update user events: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteUserEvents(Integer id) {
        logger.debug("Deleting UserEvents with id: {}", id);
        try {
            Optional<UserEvents> userEvents = userEventsRepository.findById(id);
            if (userEvents.isEmpty()) {
                logger.error("UserEvents not found for id: {}", id);
                throw new RuntimeException("UserEvents not found");
            }
            userEventsRepository.deleteById(id);
            logger.info("Deleted UserEvents with id: {}", id);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting UserEvents with id: {}", id, e);
            throw new RuntimeException("Failed to delete user events: " + e.getMessage());
        }
    }

    private void validateUserEvents(UserEvents userEvents) {
        if (userEvents.getId() == null) {
            throw new IllegalArgumentException("UserEvents ID is required");
        }
        // Add additional validation as needed, e.g., checking for required fields like userId or eventId
        if (userEvents.getUser() != null || userEvents.getEvent() != null) {
            throw new IllegalArgumentException("User ID and Event ID are required");
        }
    }
}