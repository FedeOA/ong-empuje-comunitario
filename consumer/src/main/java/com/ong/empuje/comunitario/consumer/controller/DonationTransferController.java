package com.ong.empuje.comunitario.consumer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ong.empuje.comunitario.consumer.dto.in.DonationTransferDTO;
import com.ong.empuje.comunitario.consumer.dto.in.DonationTransferItemDTO;
import com.ong.empuje.comunitario.consumer.model.DonationTransfer;
import com.ong.empuje.comunitario.consumer.model.DonationTransferItem;
import com.ong.empuje.comunitario.consumer.repository.DonationTransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final DonationTransferRepository donationTransferRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Random random = new Random();

    @Autowired
    public DonationTransferController(
            DonationTransferRepository donationTransferRepository,
            ObjectMapper objectMapper,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.donationTransferRepository = donationTransferRepository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping
    public ResponseEntity<List<DonationTransfer>> listDonationTransfers() {
        logger.info("Received GET /api/donation-transfers");
        try {
            List<DonationTransfer> transfers = donationTransferRepository.findAll()
                    .stream()
                    .filter(transfer -> !transfer.isProcessed())
                    .toList();
            logger.info("Fetched {} donation transfers", transfers.size());
            return ResponseEntity.ok(transfers);
        } catch (Exception e) {
            logger.error("Error fetching donation transfers: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createDonationTransfer(@RequestBody DonationTransferDTO payload) {
        logger.info("Received POST /api/donation-transfers/create with payload: organizationId={}", payload.organizationId());
        try {
            // Generate unique transferId and requestId
            Integer newId = generateUniqueId();
            if (newId == null) {
                logger.error("Failed to generate a unique ID after multiple attempts");
                return ResponseEntity.status(500).body("Failed to generate a unique ID");
            }

            DonationTransfer transfer = new DonationTransfer();
            transfer.setTransferId(newId);
            transfer.setRequestId(newId); // Use same ID for both, adjust if they must differ
            transfer.setOrganizationId(payload.organizationId());
            transfer.setReceived(false);
            transfer.setProcessed(false);
            transfer.setCreatedAt(LocalDateTime.now());

            for (DonationTransferItemDTO itemDTO : payload.items()) {
                DonationTransferItem item = new DonationTransferItem();
                item.setCategoryId(itemDTO.categoryId());
                item.setDescription(itemDTO.description());
                item.setQuantity(itemDTO.quantity());
                // Remove if created_at column does not exist in donation_transfer_item
                item.setCreatedAt(LocalDateTime.now());
                transfer.addItem(item);
            }

            donationTransferRepository.save(transfer);
            logger.info("Donation transfer created: transferId={}", transfer.getTransferId());

            // Update payload with generated IDs for Kafka
            DonationTransferDTO responsePayload = new DonationTransferDTO(
                newId, // request_id
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
    public ResponseEntity<String> updateDonationTransfer(@PathVariable Integer id, @RequestBody DonationTransferDTO payload) {
        logger.info("Received PUT /api/donation-transfers/{} with payload: organizationId={}, requestId={}",
                id, payload.organizationId(), payload.requestId());
        try {
            Optional<DonationTransfer> optionalTransfer = donationTransferRepository.findById(id);
            if (!optionalTransfer.isPresent()) {
                logger.warn("Donation transfer not found: id={}", id);
                return ResponseEntity.status(404).body("Donation transfer not found");
            }

            DonationTransfer transfer = optionalTransfer.get();
            // Check if transfer_id is being changed and ensure no duplicate
            if (transfer.getTransferId() != payload.requestId()) {
                Optional<DonationTransfer> existingTransfer = donationTransferRepository.findByTransferId(payload.requestId());
                if (existingTransfer.isPresent()) {
                    logger.warn("Duplicate transfer_id found: {}", payload.requestId());
                    return ResponseEntity.status(400).body("Duplicate transfer_id: " + payload.requestId());
                }
            }

            transfer.setTransferId(payload.requestId());
            transfer.setOrganizationId(payload.organizationId());
            transfer.setRequestId(payload.requestId() > 0 ? payload.requestId() : null);
            transfer.setCreatedAt(LocalDateTime.now());

            transfer.getItems().clear();
            for (DonationTransferItemDTO itemDTO : payload.items()) {
                DonationTransferItem item = new DonationTransferItem();
                item.setCategoryId(itemDTO.categoryId());
                item.setDescription(itemDTO.description());
                item.setQuantity(itemDTO.quantity());
                // Remove if created_at column does not exist
                item.setCreatedAt(LocalDateTime.now());
                transfer.addItem(item);
            }

            donationTransferRepository.save(transfer);
            logger.info("Donation transfer updated: id={}", id);

            String jsonPayload = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send("modificar-transferencia-donacion", jsonPayload);

            return ResponseEntity.ok("Donation transfer updated successfully");
        } catch (Exception e) {
            logger.error("Error updating donation transfer: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error updating donation transfer: " + e.getMessage());
        }
    }

    @PatchMapping("/{transferId}")
    public ResponseEntity<String> deleteDonationTransfer(@PathVariable Integer transferId) {
        logger.info("Received PATCH /api/donation-transfers/{}", transferId);
        try {
            Optional<DonationTransfer> optionalTransfer = donationTransferRepository.findByTransferId(transferId);
            if (!optionalTransfer.isPresent()) {
                logger.warn("Donation transfer not found: transferId={}", transferId);
                return ResponseEntity.status(404).body("Donation transfer not found");
            }

            DonationTransfer transfer = optionalTransfer.get();
            transfer.setProcessed(true);
            donationTransferRepository.save(transfer);
            logger.info("Donation transfer deleted: transferId={}", transferId);

            String jsonPayload = objectMapper.writeValueAsString(transfer);
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
            // Generate a random ID between 1 and 100000 (adjust range as needed)
            Integer newId = random.nextInt(100000) + 1;
            Optional<DonationTransfer> existingTransfer = donationTransferRepository.findByTransferId(newId);
            if (!existingTransfer.isPresent()) {
                return newId;
            }
        }
        return null; // Return null if no unique ID is found after max attempts
    }
}