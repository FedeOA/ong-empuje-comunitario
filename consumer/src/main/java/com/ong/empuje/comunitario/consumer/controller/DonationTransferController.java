package com.ong.empuje.comunitario.consumer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ong.empuje.comunitario.consumer.dto.in.DonationTransferDTO;
import com.ong.empuje.comunitario.consumer.dto.in.DonationTransferItemDTO;
import com.ong.empuje.comunitario.consumer.model.DonationTransfer;
import com.ong.empuje.comunitario.consumer.model.DonationTransferItem;
import com.ong.empuje.comunitario.consumer.service.DonationTransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/donation-transfers")
@CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH})
public class DonationTransferController {
    private static final Logger logger = LoggerFactory.getLogger(DonationTransferController.class);
    private final DonationTransferService donationTransferService;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Random random = new Random();

    public DonationTransferController(
            DonationTransferService donationTransferService,
            ObjectMapper objectMapper,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.donationTransferService = donationTransferService;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<DonationTransfer>> listDonationTransfers() {
        logger.info("Received GET /api/donation-transfers");
        try {
            List<DonationTransfer> transfers = donationTransferService.findAll();
            logger.info("Fetched {} donation transfers", transfers.size());
            return ResponseEntity.ok(transfers);
        } catch (Exception e) {
            logger.error("Error fetching donation transfers: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<String> createDonationTransfer(@RequestBody DonationTransferDTO payload) {
        logger.info("Received POST /api/donation-transfers/create with payload: organizationId={}", payload.organizationId());
        try {
            Integer newId = generateUniqueId();
            if (newId == null) {
                logger.error("Failed to generate a unique ID after multiple attempts");
                return ResponseEntity.status(500).body("Failed to generate a unique ID");
            }

            DonationTransfer transfer = new DonationTransfer();
            transfer.setTransferId(newId);
            transfer.setRequestId(newId);
            transfer.setOrganizationId(payload.organizationId());
            transfer.setReceived(false);
            transfer.setProcessed(false);
            transfer.setCreatedAt(LocalDateTime.now());

            for (DonationTransferItemDTO itemDTO : payload.items()) {
                DonationTransferItem item = new DonationTransferItem();
                item.setCategoryId(itemDTO.categoryId());
                item.setDescription(itemDTO.description());
                item.setQuantity(itemDTO.quantity());
                item.setCreatedAt(LocalDateTime.now());
                transfer.addItem(item);
            }

            donationTransferService.save(transfer);
            logger.info("Donation transfer created: transferId={}", transfer.getTransferId());

            DonationTransferDTO responsePayload = new DonationTransferDTO(
                newId,
                payload.organizationId(),
                payload.items()
            );
            String jsonPayload = objectMapper.writeValueAsString(responsePayload);
            kafkaTemplate.send("alta-transferencia-donacion", jsonPayload);

            return ResponseEntity.ok("Donation transfer created successfully with ID: " + newId);
        } catch (Exception e) {
            logger.error("Error creating donation transfer: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error creating donation transfer: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<String> updateDonationTransfer(@PathVariable Integer id, @RequestBody DonationTransferDTO payload) {
        logger.info("Received PUT /api/donation-transfers/{} with payload: organizationId={}, requestId={}",
                id, payload.organizationId(), payload.requestId());
        try {
            // Validate requestId
            if (payload.requestId() <= 0) {
                logger.warn("Invalid requestId: {}", payload.requestId());
                return ResponseEntity.status(400).body("Invalid request_id: must be a positive integer");
            }

            Optional<DonationTransfer> optionalTransfer = donationTransferService.findById(id);
            if (!optionalTransfer.isPresent()) {
                logger.warn("Donation transfer not found: id={}", id);
                return ResponseEntity.status(404).body("Donation transfer not found");
            }

            DonationTransfer transfer = optionalTransfer.get();
            // Check for duplicate transfer_id, excluding the current transfer
            if (!transfer.getTransferId().equals(payload.requestId())) {
                Optional<DonationTransfer> existingTransfer = donationTransferService.findByTransferId(payload.requestId());
                if (existingTransfer.isPresent() && !existingTransfer.get().getId().equals(id)) {
                    logger.warn("Duplicate transfer_id found: {}", payload.requestId());
                    return ResponseEntity.status(400).body("Duplicate transfer_id: " + payload.requestId());
                }
            }

            // Update scalar fields
            transfer.setTransferId(payload.requestId());
            transfer.setOrganizationId(payload.organizationId());
            transfer.setRequestId(payload.requestId() > 0 ? payload.requestId() : null);
            transfer.setCreatedAt(LocalDateTime.now());

            // Update items: Clear existing items and add new ones
            transfer.getItems().clear();
            for (DonationTransferItemDTO itemDTO : payload.items()) {
                DonationTransferItem item = new DonationTransferItem();
                item.setCategoryId(itemDTO.categoryId());
                item.setDescription(itemDTO.description());
                item.setQuantity(itemDTO.quantity());
                item.setCreatedAt(LocalDateTime.now());
                item.setTransfer(transfer);
                transfer.getItems().add(item);
            }

            donationTransferService.save(transfer);
            logger.info("Donation transfer updated: id={}", id);

            // Send Kafka message with updated payload
            DonationTransferDTO responsePayload = new DonationTransferDTO(
                transfer.getTransferId(),
                transfer.getOrganizationId(),
                transfer.getItems().stream()
                    .map(item -> new DonationTransferItemDTO(
                        item.getId(),
                        item.getCategoryId(),
                        item.getDescription(),
                        item.getQuantity()
                    ))
                    .toList()
            );
            String jsonPayload = objectMapper.writeValueAsString(responsePayload);
            kafkaTemplate.send("modificar-transferencia-donacion", jsonPayload);

            return ResponseEntity.ok("Donation transfer updated successfully");
        } catch (Exception e) {
            logger.error("Error updating donation transfer: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error updating donation transfer: " + e.getMessage());
        }
    }

    @PatchMapping("/{transferId}")
    @Transactional
    public ResponseEntity<String> deleteDonationTransfer(@PathVariable Integer transferId) {
        logger.info("Received PATCH /api/donation-transfers/{}", transferId);
        try {
            Optional<DonationTransfer> optionalTransfer = donationTransferService.findByTransferId(transferId);
            if (!optionalTransfer.isPresent()) {
                logger.warn("Donation transfer not found: transferId={}", transferId);
                return ResponseEntity.status(404).body("Donation transfer not found");
            }

            DonationTransfer transfer = optionalTransfer.get();
            transfer.setProcessed(true);
            donationTransferService.save(transfer);
            logger.info("Donation transfer deleted: transferId={}", transferId);

            DonationTransferDTO payload = new DonationTransferDTO(
                transfer.getTransferId(),
                transfer.getOrganizationId(),
                transfer.getItems().stream()
                    .map(item -> new DonationTransferItemDTO(
                        item.getId(),
                        item.getCategoryId(),
                        item.getDescription(),
                        item.getQuantity()
                    ))
                    .toList()
            );
            String jsonPayload = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send("baja-transferencia-donacion", jsonPayload);

            return ResponseEntity.ok("Donation transfer deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting donation transfer: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error deleting donation transfer: " + e.getMessage());
        }
    }

    private Integer generateUniqueId() {
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            Integer newId = random.nextInt(100000) + 1;
            Optional<DonationTransfer> existingTransfer = donationTransferService.findByTransferId(newId);
            if (!existingTransfer.isPresent()) {
                return newId;
            }
        }
        return null;
    }
}