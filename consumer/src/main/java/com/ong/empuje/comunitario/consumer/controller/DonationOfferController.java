package com.ong.empuje.comunitario.consumer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ong.empuje.comunitario.consumer.model.DonationOffer;
import com.ong.empuje.comunitario.consumer.repository.DonationOfferRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import com.ong.empuje.comunitario.consumer.dto.payload.CancelDonationOfferPayload;
import com.ong.empuje.comunitario.consumer.dto.payload.DonationOfferItemPayload;
import com.ong.empuje.comunitario.consumer.dto.payload.DonationOfferPayload;
import com.ong.empuje.comunitario.consumer.model.DonationOfferItem;

@RestController
@RequestMapping("/api/donation-offers")
public class DonationOfferController {
    private static final Logger logger = LoggerFactory.getLogger(DonationOfferController.class);
    private final DonationOfferRepository donationOfferRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public DonationOfferController(
            DonationOfferRepository donationOfferRepository,
            ObjectMapper objectMapper,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.donationOfferRepository = donationOfferRepository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping
    public ResponseEntity<List<DonationOffer>> listDonationOffers() {
        logger.info("Received GET /api/donation-offers");
        try {
            LocalDateTime now = LocalDateTime.now();
            List<DonationOffer> offers = donationOfferRepository.findByAvailableTrueAndExpiresAtAfter(now);
            logger.info("Fetched {} donation offers", offers.size());
            return ResponseEntity.ok(offers);
        } catch (Exception e) {
            logger.error("Error fetching donation offers: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createDonationOffer(@RequestBody DonationOfferPayload payload) {
        logger.info("Received POST /api/donation-offers/create with payload: organizationId={}, offerId={}",
                payload.getOrganizationId(), payload.getOfferId());
        try {
            DonationOffer offer = new DonationOffer();
            offer.setOfferId(payload.getOfferId());
            offer.setOrganizationId(payload.getOrganizationId());
            offer.setAvailable(true);
            offer.setCreatedAt(LocalDateTime.now());
            offer.setExpiresAt(payload.getExpiresAtAsLocalDateTime() != null ? 
                payload.getExpiresAtAsLocalDateTime() : LocalDateTime.now().plusDays(30));

            for (DonationOfferItemPayload itemPayload : payload.getItems()) {
                DonationOfferItem item = new DonationOfferItem();
                item.setCategoryId(itemPayload.getCategoryId());
                item.setDescription(itemPayload.getDescription());
                item.setQuantity(itemPayload.getQuantity());
                item.setCreatedAt(LocalDateTime.now());
                offer.addItem(item);
            }

            donationOfferRepository.save(offer);
            logger.info("Donation offer created: offerId={}", offer.getOfferId());

            String jsonPayload = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send("alta-oferta-donacion", jsonPayload);

            return ResponseEntity.ok("Donation offer created successfully");
        } catch (Exception e) {
            logger.error("Error creating donation offer: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error creating donation offer: " + e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelDonationOffer(@RequestBody CancelDonationOfferPayload payload) {
        logger.info("Received POST /api/donation-offers/cancel with offerId={}, organizationId={}",
                payload.getOfferId(), payload.getOrganizationId());
        try {
            DonationOffer offer = donationOfferRepository.findByOfferIdAndOrganizationId(payload.getOfferId(), payload.getOrganizationId())
                    .orElseThrow(() -> new IllegalArgumentException("Donation offer not found"));
            offer.setAvailable(false);
            donationOfferRepository.save(offer);
            logger.info("Donation offer cancelled: offerId={}", offer.getOfferId());

            String jsonPayload = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send("baja-oferta-donacion", jsonPayload);

            return ResponseEntity.ok("Donation offer cancelled successfully");
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage(), e);
            return ResponseEntity.status(404).body("Donation offer not found");
        } catch (Exception e) {
            logger.error("Error cancelling donation offer: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error cancelling donation offer: " + e.getMessage());
        }
    }
}