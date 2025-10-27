package com.grpc.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpc.demo.dto.in.DonationCancellationDTO;
import com.grpc.demo.dto.in.DonationRequestDTO;
import com.grpc.demo.enums.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka/publish")
public class KafkaController {
    private static final Logger logger = LoggerFactory.getLogger(KafkaController.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaController(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/donation-request")
    public ResponseEntity<String> publishDonationRequest(@RequestBody DonationRequestDTO request) {
        logger.debug("Received POST /kafka/publish/donation-request");
        try {
            String message = objectMapper.writeValueAsString(request);
            logger.debug("Publishing message to {}: {}", Topic.SOLICITUD_DONACIONES.getName(), message);
            kafkaTemplate.send(Topic.SOLICITUD_DONACIONES.getName(), message);
            return ResponseEntity.ok("Donation request published");
        } catch (Exception e) {
            logger.error("Error publishing donation request: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error publishing donation request");
        }
    }

    @PostMapping("/donation-cancellation")
    public ResponseEntity<String> publishDonationCancellation(@RequestBody DonationCancellationDTO request) {
        logger.debug("Received POST /kafka/publish/donation-cancellation");
        try {
            String message = objectMapper.writeValueAsString(request);
            logger.debug("Publishing message to {}: {}", Topic.BAJA_SOLICITUD_DONACIONES.getName(), message);
            kafkaTemplate.send(Topic.BAJA_SOLICITUD_DONACIONES.getName(), message);
            return ResponseEntity.ok("Donation cancellation published");
        } catch (Exception e) {
            logger.error("Error publishing donation cancellation: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error publishing donation cancellation");
        }
    }
}