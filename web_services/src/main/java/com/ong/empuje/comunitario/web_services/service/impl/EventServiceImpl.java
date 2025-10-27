package com.ong.empuje.comunitario.web_services.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ong.empuje.comunitario.web_services.dto.in.DonationDTO;
import com.ong.empuje.comunitario.web_services.dto.in.EventFilterDTO;
import com.ong.empuje.comunitario.web_services.dto.out.EventFilterResponseDTO;
import com.ong.empuje.comunitario.web_services.dto.out.EventsDonationsResponseDTO;
import com.ong.empuje.comunitario.web_services.enums.DonationDistributionFilter;
import com.ong.empuje.comunitario.web_services.repository.EventJdbcRepository;
import com.ong.empuje.comunitario.web_services.service.EventService;

@Service
public class EventServiceImpl implements EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventJdbcRepository eventRepository;

    public EventServiceImpl(EventJdbcRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public List<EventsDonationsResponseDTO> getParticipationEvents(
            String username,
            String startDate,
            String endDate,
            DonationDistributionFilter distribution
    ) {
        logger.debug("Fetching events for username: {}, startDate: {}, endDate: {}, distribution: {}",
                username, startDate, endDate, distribution);
        List<EventsDonationsResponseDTO> events = eventRepository.findFilteredEvents(username, startDate, endDate, distribution);
        logger.debug("Found {} events", events.size());
        events.forEach(event -> logger.debug("Event: {}, Donations: {}", event.getName(), event.getDonations()));
        return events.stream()
                .map(event -> {
                    EventsDonationsResponseDTO dto = new EventsDonationsResponseDTO();
                    dto.setName(event.getName());
                    dto.setDate(event.getDate());
                    dto.setDescription(event.getDescription());
                    List<DonationDTO> donationDTOs = (event.getDonations() != null)
                            ? event.getDonations().stream()
                                .filter(donation -> donation.getCategory() != null && !donation.getCategory().isEmpty())
                                .map(donation -> {
                                    DonationDTO donationDTO = new DonationDTO();
                                    donationDTO.setCategory(donation.getCategory());
                                    donationDTO.setDescription(donation.getDescription());
                                    donationDTO.setQuantity(donation.getQuantity());
                                    return donationDTO;
                                })
                                .collect(Collectors.toList())
                            : Collections.emptyList();
                    dto.setDonations(donationDTOs);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EventFilterResponseDTO> getFilters(String username) {
        return eventRepository.getFilters(username);
    }

    @Override
    public void saveFilter(EventFilterDTO eventFilter) {
        eventRepository.saveFilter(eventFilter);
    }
}