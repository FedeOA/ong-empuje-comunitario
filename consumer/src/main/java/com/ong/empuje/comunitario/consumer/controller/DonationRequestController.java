package com.ong.empuje.comunitario.consumer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ong.empuje.comunitario.consumer.model.DonationRequest;
import com.ong.empuje.comunitario.consumer.model.DonationRequestItem;
import com.ong.empuje.comunitario.consumer.repository.DonationRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/donation-requests")
public class DonationRequestController {
    private static final Logger logger = LoggerFactory.getLogger(DonationRequestController.class);
    private final DonationRequestRepository donationRequestRepository;
    private final ObjectMapper objectMapper;

    public DonationRequestController(DonationRequestRepository donationRequestRepository, ObjectMapper objectMapper) {
        this.donationRequestRepository = donationRequestRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<List<DonationRequest>> listDonationRequests() {
        logger.info("Received GET /api/donation-requests");
        try {
            List<DonationRequest> requests = donationRequestRepository.findAll();
            logger.info("Fetched {} donation requests", requests.size());
            for (DonationRequest request : requests) {
                logger.debug("Raw donation request: organizationId={}, requestId={}, deleted={}",
                        request.getOrganizationId(), request.getRequestId(), request.getDeleted());
                for (DonationRequestItem item : request.getItems()) {
                    logger.debug("Raw item: id={}, requestId={}, organizationId={}, categoryId={}, description={}",
                            item.getId(), item.getRequestId(), item.getOrganizationId(),
                            item.getCategoryId(), item.getDescription());
                }
            }
            try {
                String jsonResponse = objectMapper.writeValueAsString(requests);
                logger.debug("JSON response: {}", jsonResponse);
            } catch (Exception e) {
                logger.error("Error serializing donation requests to JSON: {}", e.getMessage(), e);
            }
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            logger.error("Error fetching donation requests: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}