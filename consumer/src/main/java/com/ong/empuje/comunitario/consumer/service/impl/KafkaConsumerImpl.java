// consumer\src\main\java\com\ong\empuje\comunitario\consumer\service\impl\KafkaConsumerImpl.java

package com.ong.empuje.comunitario.consumer.service.impl;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ong.empuje.comunitario.consumer.dto.in.DonationCancellationDTO;
import com.ong.empuje.comunitario.consumer.dto.in.DonationRequestDTO;
import com.ong.empuje.comunitario.consumer.dto.in.DonationRequestItemDTO;
import com.ong.empuje.comunitario.consumer.dto.in.EventDTO;
import com.ong.empuje.comunitario.consumer.dto.in.EventVoluntaryDTO;
import com.ong.empuje.comunitario.consumer.mapper.EventMapper;
import com.ong.empuje.comunitario.consumer.model.DonationRequest;
import com.ong.empuje.comunitario.consumer.model.DonationRequestId;
import com.ong.empuje.comunitario.consumer.model.DonationRequestItem;
import com.ong.empuje.comunitario.consumer.model.Event;
import com.ong.empuje.comunitario.consumer.model.Organization;
import com.ong.empuje.comunitario.consumer.repository.DonationRequestRepository;
import com.ong.empuje.comunitario.consumer.repository.EventRepository;
import com.ong.empuje.comunitario.consumer.repository.OrganizationRepository;
import com.ong.empuje.comunitario.consumer.mapper.VoluntaryMapper;
import com.ong.empuje.comunitario.consumer.model.*;
import com.ong.empuje.comunitario.consumer.repository.*;
import com.ong.empuje.comunitario.consumer.service.IConsumer;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Date;

@Service
public class KafkaConsumerImpl implements IConsumer {

    private final EventRepository eventRepository;
    private final DonationRequestRepository donationRequestRepository;
    private final OrganizationRepository organizationRepository;
    private final VoluntaryRepository voluntaryRepository;
    private final VoluntaryEventsRepository voluntaryEventsRepository;
    private final UserRepository userRepository;
    private final UserEventsRepository userEventsRepository;
    private final ObjectMapper objectMapper;
    private static final int MAX_ID_GENERATION_ATTEMPTS = 3;
    private static final List<Integer> VALID_CATEGORIES = Arrays.asList(1, 2, 3, 4); // ALIMENTOS, ROPA, JUGUETES, UTILES_ESCOLARES
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerImpl.class);

    public KafkaConsumerImpl(EventRepository eventRepository,
                             OrganizationRepository organizationRepository,
                             VoluntaryRepository voluntaryRepository,
                             VoluntaryEventsRepository registrationEventsRepository,
                             ObjectMapper objectMapper, UserRepository userRepository,
                             UserEventsRepository userEventsRepository,
                             DonationRequestRepository donationRequestRepository) {
        this.eventRepository = eventRepository;
        this.donationRequestRepository = donationRequestRepository;
        this.organizationRepository = organizationRepository;
        this.voluntaryRepository = voluntaryRepository;
        this.voluntaryEventsRepository = registrationEventsRepository;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.userEventsRepository = userEventsRepository;
    }

    @Override
    @KafkaListener(topics = "eventos-solidarios", groupId = "consumidor1")
    public void listenCreateEvents(String message) {
        try {
            EventDTO event = objectMapper.readValue(message, EventDTO.class);
            eventRepository.save(buildEvent(event));
        } catch (Exception e) {
            logger.error("Exception in event listener: {} - {}", e.getCause(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "solicitud_donaciones", groupId = "ong-empuje-comunitario")
    @Transactional
    @Override
    public void listenDonationRequests(String message) {
        try {
            logger.info("Received message from solicitud_donaciones: {}", message);
            DonationRequestDTO dto = objectMapper.readValue(message, DonationRequestDTO.class);
            logger.debug("Parsed DTO: requestId={}, organizationId={}, items={}",
                    dto.getRequestId(), dto.getOrganizationId(), dto.getItems());

            if (dto.getOrganizationId() == null) {
                logger.error("Invalid donation request: organizationId is null");
                return;
            }

            if (dto.getItems() == null || dto.getItems().isEmpty()) {
                logger.error("Invalid donation request: items list is null or empty");
                return;
            }

            Integer requestId = dto.getRequestId();
            DonationRequestId id = new DonationRequestId();
            id.setOrganizationId(dto.getOrganizationId());

            // Generate random requestId if not provided
            if (requestId == null) {
                for (int attempt = 1; attempt <= MAX_ID_GENERATION_ATTEMPTS; attempt++) {
                    requestId = ThreadLocalRandom.current().nextInt(1, 1000000); // Random ID between 1 and 999999
                    id.setRequestId(requestId);
                    if (!donationRequestRepository.findById(id).isPresent()) {
                        logger.info("Generated unique requestId: {}", requestId);
                        break;
                    }
                    logger.warn("Generated requestId {} already exists, retrying (attempt {}/{})", requestId, attempt, MAX_ID_GENERATION_ATTEMPTS);
                    if (attempt == MAX_ID_GENERATION_ATTEMPTS) {
                        logger.error("Failed to generate unique requestId after {} attempts", MAX_ID_GENERATION_ATTEMPTS);
                        return;
                    }
                }
            } else {
                id.setRequestId(requestId);
                // Check if the provided requestId already exists
                if (donationRequestRepository.findById(id).isPresent()) {
                    logger.warn("Donation request already exists: requestId={}, organizationId={}",
                            requestId, dto.getOrganizationId());
                    return; // Idempotent: skip if already processed
                }
            }

            DonationRequest donationRequest = new DonationRequest();
            donationRequest.setRequestId(requestId);
            donationRequest.setOrganizationId(dto.getOrganizationId());
            donationRequest.setDeleted(false);

            for (DonationRequestItemDTO itemDTO : dto.getItems()) {
                if (itemDTO.getCategoryId() == null || !VALID_CATEGORIES.contains(itemDTO.getCategoryId())) {
                    logger.error("Invalid item: categoryId is null or invalid ({})", itemDTO.getCategoryId());
                    return;
                }
                if (itemDTO.getDescription() == null || itemDTO.getDescription().trim().isEmpty()) {
                    logger.error("Invalid item: description is null or empty");
                    return;
                }

                DonationRequestItem item = new DonationRequestItem();
                item.setRequestId(requestId);
                item.setOrganizationId(dto.getOrganizationId());
                item.setCategoryId(itemDTO.getCategoryId());
                item.setDescription(itemDTO.getDescription());
                donationRequest.getItems().add(item);
            }

            donationRequestRepository.save(donationRequest);
            logger.info("Saved donation request: requestId={}, organizationId={}",
                    donationRequest.getRequestId(), donationRequest.getOrganizationId());
        } catch (Exception e) {
            logger.error("Error processing donation request message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process donation request", e);
        }
    }

    @KafkaListener(topics = "baja_solicitud_donaciones", groupId = "ong-empuje-comunitario")
    @Transactional
    @Override
    public void listenDonationCancellations(String message) {
        try {
            logger.info("Received message from baja_solicitud_donaciones: {}", message);
            DonationCancellationDTO dto = objectMapper.readValue(message, DonationCancellationDTO.class);
            if (dto.getRequestId() == null || dto.getOrganizationId() == null) {
                logger.error("Invalid cancellation request: requestId or organizationId is null");
                return;
            }
            DonationRequestId id = new DonationRequestId();
            id.setRequestId(dto.getRequestId());
            id.setOrganizationId(dto.getOrganizationId());
            Optional<DonationRequest> existingRequest = donationRequestRepository.findById(id);
            if (existingRequest.isPresent()) {
                DonationRequest request = existingRequest.get();
                request.setDeleted(true);
                donationRequestRepository.save(request);
                logger.info("Marked donation request as deleted: requestId={}, organizationId={}",
                        dto.getRequestId(), dto.getOrganizationId());
            } else {
                logger.warn("No donation request found to mark as deleted: requestId={}, organizationId={}",
                        dto.getRequestId(), dto.getOrganizationId());
            }
        } catch (Exception e) {
            logger.error("Error processing donation cancellation message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process donation cancellation", e);
        }
    }

    @KafkaListener(topics = "alta-solicitud-donacion", groupId = "ong-empuje-comunitario")
    @Transactional
    public void consumeDonationRequest(String message) {
        try {
            logger.info("Received message from solicitud_donaciones: {}", message);
            DonationRequestDTO dto = objectMapper.readValue(message, DonationRequestDTO.class);
            logger.debug("Parsed DTO: requestId={}, organizationId={}, items={}",
                    dto.getRequestId(), dto.getOrganizationId(), dto.getItems());

            if (dto.getRequestId() == null || dto.getOrganizationId() == null) {
                logger.error("Invalid donation request: requestId or organizationId is null");
                return;
            }

            if (dto.getItems() == null || dto.getItems().isEmpty()) {
                logger.error("Invalid donation request: items list is null or empty");
                return;
            }

            // Check if the donation request already exists
            DonationRequestId id = new DonationRequestId();
            id.setRequestId(dto.getRequestId());
            id.setOrganizationId(dto.getOrganizationId());
            Optional<DonationRequest> existingRequest = donationRequestRepository.findById(id);
            if (existingRequest.isPresent()) {
                logger.warn("Donation request already exists: requestId={}, organizationId={}",
                        dto.getRequestId(), dto.getOrganizationId());
                return; // Idempotent: skip if already processed
            }

            DonationRequest donationRequest = new DonationRequest();
            donationRequest.setRequestId(dto.getRequestId());
            donationRequest.setOrganizationId(dto.getOrganizationId());
            donationRequest.setDeleted(false);

            for (DonationRequestItemDTO itemDTO : dto.getItems()) {
                if (itemDTO.getCategoryId() == null || !VALID_CATEGORIES.contains(itemDTO.getCategoryId())) {
                    logger.error("Invalid item: categoryId is null or invalid ({})", itemDTO.getCategoryId());
                    return;
                }
                if (itemDTO.getDescription() == null || itemDTO.getDescription().trim().isEmpty()) {
                    logger.error("Invalid item: description is null or empty");
                    return;
                }

                DonationRequestItem item = new DonationRequestItem();
                item.setRequestId(dto.getRequestId());
                item.setOrganizationId(dto.getOrganizationId());
                item.setCategoryId(itemDTO.getCategoryId());
                item.setDescription(itemDTO.getDescription());
                donationRequest.getItems().add(item);
            }

            donationRequestRepository.save(donationRequest);
            logger.info("Saved donation request: requestId={}, organizationId={}",
                    donationRequest.getRequestId(), donationRequest.getOrganizationId());
        } catch (JsonProcessingException e) {
            logger.error("Error processing donation request message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process donation request", e); // Ensure transaction rollback
        }
    }

    @KafkaListener(topics = "alta-solicitud-donacion", groupId = "ong-empuje-comunitario")
    @Transactional
    public void consumeDonationCancellation(String message) {
        try {
            logger.info("Received message from baja_solicitud_donaciones: {}", message);
            DonationRequestDTO dto = objectMapper.readValue(message, DonationRequestDTO.class);
            if (dto.getRequestId() == null || dto.getOrganizationId() == null) {
                logger.error("Invalid cancellation request: requestId or organizationId is null");
                return;
            }
            int updated = donationRequestRepository.setDeletedByRequestIdAndOrganizationId(dto.getRequestId(), dto.getOrganizationId());
            if (updated > 0) {
                logger.info("Marked donation request as deleted: requestId={}, organizationId={}",
                        dto.getRequestId(), dto.getOrganizationId());
            } else {
                logger.warn("No donation request found to mark as deleted: requestId={}, organizationId={}",
                        dto.getRequestId(), dto.getOrganizationId());
            }
        } catch (JsonProcessingException e) {
            logger.error("Error processing donation cancellation message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process donation cancellation", e);
        }
    }


    private Event buildEvent(EventDTO message) throws Exception {
        Optional<Organization> organization = organizationRepository.findById(Integer.valueOf(message.organizationId()));

        if (organization.isPresent() && organization.get().getId() != 1) {
            Event event = EventMapper.INSTANCE.toEntity(message);
            event.setOrganization(organization.get());
            return event;
        } else {
            throw new Exception("no existe la organizacion");
        }
    }

    @Override
    @KafkaListener(topics = "baja-evento-solidario", groupId = "consumidor2")
    public void listenDeleteEvents(String message) {

        try {
            EventDTO deleteEvent = objectMapper.readValue(message,EventDTO.class);

            if(Integer.parseInt(deleteEvent.organizationId()) != 1) { // Id de organizacion propia
                eventRepository.deleteByRemoteIdAndOrganizationId(deleteEvent.eventId(), Integer.valueOf(deleteEvent.organizationId()));
            }

            }catch (Exception e){
            System.out.println("Exception : "+ e.getCause() + e.getMessage());
        }
    }

    @Override
    @KafkaListener(topics = "adhesion-evento",groupId = "consumidor3")
    public void listenAddVoluntary(String message) {

        try {

            EventVoluntaryDTO eventVoluntary = objectMapper.readValue(message, EventVoluntaryDTO.class);
            Voluntary voluntary = VoluntaryMapper.INSTANCE.toEntity(eventVoluntary.voluntary());

            Optional<Event> event = eventRepository.findByRemoteId(eventVoluntary.remoteId());

            if (event.isPresent()) {
                if (eventVoluntary.originOrganizationId() == 1) { // evento de mi organización

                    Voluntary toSave;

                    Optional<Voluntary> toUpdate = voluntaryRepository
                            .findByOrganizationIdAndVoluntaryId(voluntary.getOrganizationId(), voluntary.getVoluntaryId());

                    if (toUpdate.isPresent()) {
                        Voluntary existing = toUpdate.get();
                        existing.setName(voluntary.getName());
                        existing.setLastName(voluntary.getLastName());
                        existing.setPhone(voluntary.getPhone());
                        existing.setEmail(voluntary.getEmail());

                        toSave = voluntaryRepository.save(existing);
                    } else {
                        toSave = voluntaryRepository.save(voluntary);
                    }

                    VoluntaryEvents voluntaryEvents = new VoluntaryEvents();
                    voluntaryEvents.setEvent(event.get());
                    voluntaryEvents.setVoluntary(toSave);
                    voluntaryEvents.setRegistrationDate(new Date());
                    voluntaryEventsRepository.save(voluntaryEvents);
                } else if (voluntary.getOrganizationId() == 1){ // usuario de mi organizacion

                    Optional<User> user = userRepository.findById(voluntary.getVoluntaryId());

                    if(user.isPresent()){
                        UserEvents userEvents = new UserEvents();
                        userEvents.setEvent(event.get());
                        userEvents.setUser(user.get());
                        userEvents.setRegistrationDate(new Date());
                        userEventsRepository.save(userEvents);
                    }
                }
        }
        }catch (Exception e){
            System.out.println("Exception : "+ e.getCause() + e.getMessage());
        }
    }

    private boolean validateOrganization(Integer orgId) {
        return organizationRepository.findById(orgId).isPresent();
    }


    private DonationRequest buildDonationRequest(DonationRequestDTO dto) {
        // Busca si ya existe la solicitud
        DonationRequest request = donationRequestRepository.findByRequestIdAndOrganizationId(
            dto.getRequestId(), dto.getOrganizationId()
        ).orElse(new DonationRequest());

        // Configura los campos
        request.setRequestId(dto.getRequestId()); // Clave primaria
        request.setOrganizationId(dto.getOrganizationId());
        request.setDeleted(false);

        // Limpia ítems existentes si es necesario
        request.getItems().clear();

        // Añade nuevos ítems
        for (var itemDto : dto.getItems()) {
            DonationRequestItem item = new DonationRequestItem();
            item.setCategoryId(itemDto.getCategoryId());
            item.setDescription(itemDto.getDescription());
            request.getItems().add(item);
        }

        return request;
    }
}