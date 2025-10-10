// consumer\src\main\java\com\ong\empuje\comunitario\consumer\service\impl\KafkaConsumerImpl.java

package com.ong.empuje.comunitario.consumer.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ong.empuje.comunitario.consumer.dto.in.DonationCancellationDTO;
import com.ong.empuje.comunitario.consumer.dto.in.DonationOfferDTO;
import com.ong.empuje.comunitario.consumer.dto.in.DonationRequestDTO;
import com.ong.empuje.comunitario.consumer.dto.in.DonationTransferDTO;
import com.ong.empuje.comunitario.consumer.dto.in.EventDTO;
import com.ong.empuje.comunitario.consumer.mapper.EventMapper;
import com.ong.empuje.comunitario.consumer.model.DonationOffer;
import com.ong.empuje.comunitario.consumer.model.DonationOfferItem;
import com.ong.empuje.comunitario.consumer.model.DonationRequest;
import com.ong.empuje.comunitario.consumer.model.DonationRequestItem;
import com.ong.empuje.comunitario.consumer.model.DonationTransfer;
import com.ong.empuje.comunitario.consumer.model.DonationTransferItem;
import com.ong.empuje.comunitario.consumer.model.Event;
import com.ong.empuje.comunitario.consumer.model.Organization;
import com.ong.empuje.comunitario.consumer.repository.DonationOfferRepository;
import com.ong.empuje.comunitario.consumer.repository.DonationRequestRepository;
import com.ong.empuje.comunitario.consumer.repository.DonationTransferRepository;
import com.ong.empuje.comunitario.consumer.repository.EventRepository;
import com.ong.empuje.comunitario.consumer.repository.OrganizationRepository;
import com.ong.empuje.comunitario.consumer.service.IConsumer;

@Service
public class KafkaConsumerImpl implements IConsumer {

    private final EventRepository eventRepository;
    private final DonationRequestRepository donationRequestRepository;
    private final OrganizationRepository organizationRepository;
    private final ObjectMapper objectMapper;
    private final DonationTransferRepository donationTransferRepository;
    private final DonationOfferRepository donationOfferRepository;

    public KafkaConsumerImpl(EventRepository eventRepository,
                             DonationRequestRepository donationRequestRepository,
                             OrganizationRepository organizationRepository,
                             DonationTransferRepository donationTransferRepository,
                             DonationOfferRepository donationOfferRepository,
                             ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.donationRequestRepository = donationRequestRepository;
        this.organizationRepository = organizationRepository;
        this.donationTransferRepository = donationTransferRepository;
        this.donationOfferRepository = donationOfferRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @KafkaListener(topics = "eventos-solidarios", groupId = "consumidor1")
    public void listenCreateEvents(String message) {
        try {
            EventDTO event = objectMapper.readValue(message, EventDTO.class);
            eventRepository.save(buildEvent(event));
        } catch (Exception e) {
            System.out.println("Exception in event listener: " + e.getCause() + e.getMessage());
        }
    }
    
    @KafkaListener(topics = "solicitud_donaciones", groupId = "consumidor1")
    @Transactional
    public void listenDonationRequests(String message) {
        try {
            DonationRequestDTO requestDto = objectMapper.readValue(message, DonationRequestDTO.class);
            if (validateOrganization(requestDto.organizationId())) {
                DonationRequest request = buildDonationRequest(requestDto);
                donationRequestRepository.save(request);
                System.out.println("Processed donation request: " + requestDto.requestId());
            }
        } catch (Exception e) {
            System.out.println("Exception in donation request listener: " + e.getCause() + e.getMessage());
        }
    }
    
    @KafkaListener(topics = "baja_solicitud_donaciones", groupId = "consumidor1")
    @Transactional
    public void listenDonationCancellations(String message) {
        try {
            DonationCancellationDTO cancellationDto = objectMapper.readValue(message, DonationCancellationDTO.class);
            Optional<DonationRequest> optionalRequest = donationRequestRepository.findByRequestIdAndOrganizationId(
                cancellationDto.requestId(), cancellationDto.organizationId()
            );
            if (optionalRequest.isPresent() && !optionalRequest.get().isDeleted()) {
                DonationRequest request = optionalRequest.get();
                request.setDeleted(true);
                donationRequestRepository.save(request);
                System.out.println("Processed donation cancellation: " + cancellationDto.requestId());
            }
        } catch (Exception e) {
            System.out.println("Exception in donation cancellation listener: " + e.getCause() + e.getMessage());
        }
    }

    @Override
    @KafkaListener(topics = "transferencia-donaciones-${ong.id:1}", groupId = "consumidor1")
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
    @KafkaListener(topics = "oferta-donaciones", groupId = "consumidor1")
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

        if (organization.isPresent()) {
            Event event = EventMapper.INSTANCE.toEntity(message);
            event.setOrganization(organization.get());
            return event;
        } else {
            throw new Exception("no existe la organizacion");
        }
    }
    
    private boolean validateOrganization(Integer orgId) {
        return organizationRepository.findById(orgId).isPresent();
    }
    

    private DonationRequest buildDonationRequest(DonationRequestDTO dto) {
        // Busca si ya existe la solicitud
        DonationRequest request = donationRequestRepository.findByRequestIdAndOrganizationId(
            dto.requestId(), dto.organizationId()
        ).orElse(new DonationRequest());

        // Configura los campos
        request.setRequestId(dto.requestId()); // Clave primaria
        request.setOrganizationId(dto.organizationId());
        request.setDeleted(false);
        request.setCreatedAt(LocalDateTime.now());

        // Limpia ítems existentes si es necesario
        request.getItems().clear();

        // Añade nuevos ítems
        for (var itemDto : dto.items()) {
            DonationRequestItem item = new DonationRequestItem();
            item.setCategoryId(itemDto.categoryId());
            item.setDescription(itemDto.description());
            item.setCreatedAt(LocalDateTime.now());
            item.setRequest(request);
            request.getItems().add(item);
        }
        
        return request;
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