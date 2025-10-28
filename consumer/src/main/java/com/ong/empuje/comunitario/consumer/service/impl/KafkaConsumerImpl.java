package com.ong.empuje.comunitario.consumer.service.impl;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.ong.empuje.comunitario.consumer.dto.in.DonationOfferDTO;
import com.ong.empuje.comunitario.consumer.dto.in.DonationRequestDTO;
import com.ong.empuje.comunitario.consumer.dto.in.DonationTransferDTO;
import com.ong.empuje.comunitario.consumer.dto.in.DonationRequestItemDTO;
import com.ong.empuje.comunitario.consumer.dto.in.EventDTO;
import com.ong.empuje.comunitario.consumer.dto.in.EventVoluntaryDTO;
import com.ong.empuje.comunitario.consumer.mapper.EventMapper;
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
    private final DonationTransferRepository donationTransferRepository;
    private final DonationOfferRepository donationOfferRepository;
    private static final int MAX_ID_GENERATION_ATTEMPTS = 3;
    private static final List<Integer> VALID_CATEGORIES = Arrays.asList(1, 2, 3, 4); // ALIMENTOS, ROPA, JUGUETES, UTILES_ESCOLARES
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerImpl.class);

    public KafkaConsumerImpl(EventRepository eventRepository,
                             OrganizationRepository organizationRepository,
                             VoluntaryRepository voluntaryRepository,
                             VoluntaryEventsRepository registrationEventsRepository,
                             ObjectMapper objectMapper,
                             UserRepository userRepository,
                             UserEventsRepository userEventsRepository,
                             DonationRequestRepository donationRequestRepository,
                             DonationTransferRepository donationTransferRepository,
                             DonationOfferRepository donationOfferRepository) {

        this.eventRepository = eventRepository;
        this.donationRequestRepository = donationRequestRepository;
        this.organizationRepository = organizationRepository;
        this.donationTransferRepository = donationTransferRepository;
        this.donationOfferRepository = donationOfferRepository;
        this.voluntaryRepository = voluntaryRepository;
        this.voluntaryEventsRepository = registrationEventsRepository;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.userEventsRepository = userEventsRepository;
    }

    @Override
    @KafkaListener(topics = "eventos-solidarios", groupId = "ong-empuje-comunitario")
    public void listenCreateEvents(String message) {
        try {
            EventDTO event = objectMapper.readValue(message, EventDTO.class);
            eventRepository.save(buildEvent(event));
        } catch (Exception e) {
            logger.error("Exception in event listener: {} - {}", e.getCause(), e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "alta-solicitud-donacion", groupId = "ong-empuje-comunitario")
    @Transactional
    public void listenDonationRequests(String message) {
        try {
            logger.info("Received message from alta-solicitud-donacion : {}", message);
            DonationRequestDTO dto = objectMapper.readValue(message, DonationRequestDTO.class);
            logger.debug("Parsed DTO: requestId={}, organizationId={}, items count={}", 
                        dto.getRequestId(), dto.getOrganizationId(), dto.getItems() != null ? dto.getItems().size() : 0);

            if (dto.getOrganizationId() == null) {
                logger.error("Invalid donation request: organizationId is null");
                return;
            }
            if (dto.getItems() == null || dto.getItems().isEmpty()) {
                logger.error("Invalid donation request: items null/empty");
                return;
            }

            Integer requestId = dto.getRequestId();
            if (requestId == null) {
                // Generate unique ID
                for (int attempt = 1; attempt <= MAX_ID_GENERATION_ATTEMPTS; attempt++) {
                    requestId = ThreadLocalRandom.current().nextInt(100000, 999999);
                    DonationRequestId tempId = new DonationRequestId(dto.getOrganizationId(), requestId);
                    if (!donationRequestRepository.findById(tempId).isPresent()) {
                        logger.info("Generated unique requestId: {}", requestId);
                        break;
                    }
                    if (attempt == MAX_ID_GENERATION_ATTEMPTS) {
                        logger.error("Failed to generate unique requestId");
                        return;
                    }
                }
            } else {
                DonationRequestId id = new DonationRequestId(dto.getOrganizationId(), requestId);
                if (donationRequestRepository.findById(id).isPresent()) {
                    logger.warn("Duplicate requestId: {}", requestId);
                    return;
                }
            }

            // Validate items
            for (DonationRequestItemDTO itemDTO : dto.getItems()) {
                if (itemDTO.getCategoryId() == null || !VALID_CATEGORIES.contains(itemDTO.getCategoryId()) ||
                    itemDTO.getDescription() == null || itemDTO.getDescription().trim().isEmpty()) {
                    logger.error("Invalid item: categoryId={}, desc={}", itemDTO.getCategoryId(), itemDTO.getDescription());
                    return;
                }
            }

            DonationRequest donationRequest = buildDonationRequest(dto, requestId);
            donationRequestRepository.saveAndFlush(donationRequest); // Immediate flush for debugging
            logger.info("Saved donation request: requestId={}, orgId={}", requestId, dto.getOrganizationId());
        } catch (JsonProcessingException e) {
            logger.error("JSON parse error in donation request: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing donation request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process donation request", e); // Rollback
        }
    }

    @KafkaListener(topics = "baja-solicitud-donacion", groupId = "ong-empuje-comunitario")
    @Transactional
    public void listenDonationCancellations(String message) {
        try {
            logger.info("Received message from baja-solicitud-donacion : {}", message);
            DonationCancellationDTO dto = objectMapper.readValue(message, DonationCancellationDTO.class);
            if (dto.getRequestId() == null || dto.getOrganizationId() == null) {
                logger.error("Invalid cancellation: requestId or organizationId is null");
                return;
            }

            DonationRequestId id = new DonationRequestId(dto.getOrganizationId(), dto.getRequestId());
            Optional<DonationRequest> existing = donationRequestRepository.findByRequestIdAndOrganizationId(dto.getRequestId(), dto.getOrganizationId());
            if (existing.isEmpty()) {
                logger.warn("No donation request found for cancellation: requestId={}, orgId={}", 
                            dto.getRequestId(), dto.getOrganizationId());
                return;
            }

            DonationRequest request = existing.get();
            if (request.getDeleted()) {
                logger.debug("Request already deleted: requestId={}, orgId={}", 
                            dto.getRequestId(), dto.getOrganizationId());
                return;
            }

            request.setDeleted(true);
            donationRequestRepository.saveAndFlush(request); // Immediate flush for debugging
            logger.info("Marked as deleted: requestId={}, orgId={}", 
                        dto.getRequestId(), dto.getOrganizationId());
        } catch (JsonProcessingException e) {
            logger.error("JSON parse error in cancellation: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing cancellation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process cancellation", e);
        }
    }

    @Override
    @KafkaListener(topics = "transferencia-donaciones-${ong.id:1}", groupId = "ong-empuje-comunitario")
    @Transactional
    public void listenDonationTransfers(String message){
        try {
            DonationTransferDTO transferDto = objectMapper.readValue(message, DonationTransferDTO.class);
            Optional<DonationTransfer> optionalTransfer = donationTransferRepository.findByTransferIdAndOrganizationId(transferDto.requestId(), transferDto.organizationId());
            if(optionalTransfer.isPresent()){
                System.out.println("Processed donation transfer: " + transferDto.requestId());
                return;
            }
            DonationTransfer transfer = buildDonationTransfer(transferDto);
            donationTransferRepository.save(transfer);


            System.out.println("Successful transfer");
        } catch (Exception e) {
            System.out.println("Exception in donation transfer listener: " + e.getCause() + e.getMessage());
        }
    }

    @Override
    @KafkaListener(topics = "oferta-donaciones", groupId = "ong-empuje-comunitario")
    @Transactional
    public void listenDonationOffers(String message){
        try {
            DonationOfferDTO offerDto = objectMapper.readValue(message, DonationOfferDTO.class);
            Optional<DonationOffer> optionalOffer = donationOfferRepository.findByOfferIdAndOrganizationId(offerDto.offerId(), offerDto.organizationId());
            if(optionalOffer.isPresent()){
                System.out.println("Processed donation offer: " + offerDto.offerId());
                return;
            }
            DonationOffer offer = buildDonationOffer(offerDto);
            donationOfferRepository.save(offer);

            System.out.println("Offer successfully registered");
        } catch (Exception e) {
            System.out.println("Exception in donation offer listener: " + e.getCause() + e.getMessage());
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
    @KafkaListener(topics = "baja-evento-solidario", groupId = "ong-empuje-comunitario")
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
    @KafkaListener(topics = "adhesion-evento",groupId = "ong-empuje-comunitario")
    public void listenAddVoluntary(String message) {

        try {

            EventVoluntaryDTO eventVoluntary = objectMapper.readValue(message, EventVoluntaryDTO.class);
            Voluntary voluntary = VoluntaryMapper.INSTANCE.toEntity(eventVoluntary.voluntary());


            if (eventVoluntary.originOrganizationId() == 1) { // evento de mi organización

                Optional<Event> event = eventRepository.findById(eventVoluntary.remoteId());
                Voluntary toSave;

                Optional<Voluntary> toUpdate = voluntaryRepository
                            .findByOrganizationIdAndVoluntaryId(voluntary.getOrganizationId(), voluntary.getVoluntaryId());

                if (toUpdate.isPresent()){
                    Voluntary existing = toUpdate.get();
                    existing.setName(voluntary.getName());
                    existing.setLastName(voluntary.getLastName());
                    existing.setPhone(voluntary.getPhone());
                    existing.setEmail(voluntary.getEmail());

                    toSave = voluntaryRepository.save(existing);
                } else {
                    toSave = voluntaryRepository.save(voluntary);
                }

                if (event.isPresent()) {
                    VoluntaryEvents voluntaryEvents = new VoluntaryEvents();
                    voluntaryEvents.setEvent(event.get());
                    voluntaryEvents.setVoluntary(toSave);
                    voluntaryEvents.setRegistrationDate(new Date());
                    voluntaryEventsRepository.save(voluntaryEvents);
                }

                } else if (voluntary.getOrganizationId() == 1){ // usuario de mi organizacion

                    Optional<Event> event = eventRepository.findByRemoteId(eventVoluntary.remoteId());
                    Optional<User> user = userRepository.findById(voluntary.getVoluntaryId());

                    if(user.isPresent() && event.isPresent()){
                        UserEvents userEvents = new UserEvents();
                        userEvents.setEvent(event.get());
                        userEvents.setUser(user.get());
                        userEvents.setRegistrationDate(new Date());
                        userEventsRepository.save(userEvents);
                    }
                }
        }catch (Exception e){
            System.out.println("Exception : "+ e.getCause() + e.getMessage());
        }
    }

    private boolean validateOrganization(Integer orgId) {
        return organizationRepository.findById(orgId).isPresent();
    }


    private DonationRequest buildDonationRequest(DonationRequestDTO dto, Integer requestId) {
        DonationRequest donationRequest = new DonationRequest();
        donationRequest.setOrganizationId(dto.getOrganizationId());
        donationRequest.setRequestId(requestId);
        donationRequest.setDeleted(false);

        List<DonationRequestItem> items = new ArrayList<>();
        for (DonationRequestItemDTO itemDTO : dto.getItems()) {
            DonationRequestItem item = new DonationRequestItem();
            item.setOrganizationId(dto.getOrganizationId());
            item.setRequestId(requestId); // Ensure requestId is set
            item.setCategoryId(itemDTO.getCategoryId());
            item.setDescription(itemDTO.getDescription());
            items.add(item);
        }
        donationRequest.setItems(items);
        logger.debug("Built donation request: requestId={}, orgId={}, items count={}", 
                    requestId, dto.getOrganizationId(), items.size());
        return donationRequest;
    }

    private DonationTransfer buildDonationTransfer(DonationTransferDTO dto){
        DonationTransfer transfer = donationTransferRepository.findByTransferIdAndOrganizationId(dto.requestId(), dto.organizationId()).orElse(new DonationTransfer());

        transfer.setTransferId(dto.requestId());
        transfer.setOrganizationId(dto.organizationId());
        transfer.setRequestId(dto.requestId());
        transfer.setCreatedAt(LocalDateTime.now());
        transfer.setReceived(true);

        transfer.getItems().clear();

        for (var itemDto : dto.items()){
            DonationTransferItem item = new DonationTransferItem();
            item.setCategoryId(itemDto.categoryId());
            item.setDescription(itemDto.description());
            item.setCreatedAt(LocalDateTime.now());
            item.setQuantity(itemDto.quantity());
            item.setTransfer(transfer);
            transfer.getItems().add(item);
        }
        return transfer;
    }

    private DonationOffer buildDonationOffer(DonationOfferDTO dto){
        DonationOffer offer = donationOfferRepository.findByOfferIdAndOrganizationId(dto.offerId(), dto.organizationId()).orElse(new DonationOffer());

        offer.setOfferId(dto.offerId());
        offer.setOrganizationId(dto.organizationId());
        offer.setAvailable(true);
        offer.setCreatedAt(LocalDateTime.now());

        offer.getItems().clear();

        for(var itemDto : dto.items()){
            DonationOfferItem item = new DonationOfferItem();
            item.setCategoryId(itemDto.categoryId());
            item.setDescription(itemDto.description());
            item.setCreatedAt(LocalDateTime.now());
            item.setQuantity(itemDto.quantity());
            item.setOffer(offer);
            offer.getItems().add(item);
        }

        return offer;
    }
}