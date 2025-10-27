package com.ong.empuje.comunitario.consumer.service.impl;

import java.util.List;

import com.ong.empuje.comunitario.consumer.model.DonationOffer;
import com.ong.empuje.comunitario.consumer.model.Event;
import com.ong.empuje.comunitario.consumer.repository.EventRepository;
import com.ong.empuje.comunitario.consumer.service.EventService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findByRemoteId(Integer remoteId) {
        logger.debug("Finding Event with remoteId: {}", remoteId);
        try {
            return eventRepository.findByRemoteId(remoteId);
        } catch (Exception e) {
            logger.error("Error finding Event with remoteId: {}", remoteId, e);
            throw new RuntimeException("Failed to find event: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Event createEvent(Event event) {
        logger.debug("Creating Event: {}", event);
        try {
            validateEvent(event);
            return eventRepository.save(event);
        } catch (Exception e) {
            logger.error("Error creating Event: {}", event, e);
            throw new RuntimeException("Failed to create event: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Event updateEvent(Event event) {
        logger.debug("Updating Event: {}", event);
        try {
            validateEvent(event);
            Optional<Event> existingEvent = eventRepository.findByRemoteId(event.getRemoteId());
            if (existingEvent.isEmpty()) {
                logger.error("Event not found for remoteId: {} and organizationId: {}", 
                    event.getRemoteId(), event.getOrganization().getId());
                throw new RuntimeException("Event not found");
            }
            Event updatedEvent = eventRepository.save(event);
            return updatedEvent;
        } catch (Exception e) {
            logger.error("Error updating Event: {}", event, e);
            throw new RuntimeException("Failed to update event: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteEvent(Integer remoteId, Integer organizationId) {
        logger.debug("Deleting Event with remoteId: {} and organizationId: {}", remoteId, organizationId);
        try {
            Optional<Event> event = eventRepository.findByRemoteId(remoteId);
            if (event.isEmpty() || !event.get().getOrganization().getId().equals(organizationId)) {
                logger.error("Event not found for remoteId: {} and organizationId: {}", remoteId, organizationId);
                throw new RuntimeException("Event not found");
            }
            eventRepository.deleteByRemoteIdAndOrganizationId(remoteId, organizationId);
            logger.info("Deleted Event with remoteId: {} and organizationId: {}", remoteId, organizationId);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting Event with remoteId: {} and organizationId: {}", remoteId, organizationId, e);
            throw new RuntimeException("Failed to delete event: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findById(Integer id) {
        logger.debug("Finding Event with id: {}", id);
        try {
            return eventRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding Event with id: {}", id, e);
            throw new RuntimeException("Failed to find event: " + e.getMessage());
        }
    }

    private void validateEvent(Event event) {
        if (event.getRemoteId() == null || event.getOrganization() == null) {
            throw new IllegalArgumentException("Remote ID and Organization ID are required");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> save(Event event) {
        logger.debug("Saving User with id: {}", event);
        try {
            return Optional.of(eventRepository.save(event));
        } catch (Exception e) {
            logger.error("Error finding User with id: {}", event.getId(), e);
            throw new RuntimeException("Failed to find user: " + e.getMessage());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<Event> findAll() {
        logger.debug("Finding all Users");
        try {
            return eventRepository.findAll();
        } catch (Exception e) {
            logger.error("Error finding all Users", e);
            throw new RuntimeException("Failed to find users: " + e.getMessage());
        }
    }
}