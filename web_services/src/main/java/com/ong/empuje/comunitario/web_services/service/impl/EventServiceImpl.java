package com.ong.empuje.comunitario.web_services.service.impl;

import com.ong.empuje.comunitario.web_services.dto.in.DonationDTO;
import com.ong.empuje.comunitario.web_services.dto.in.EventFilterDTO;
import com.ong.empuje.comunitario.web_services.dto.in.EventsDonationsResponseDTO;
import com.ong.empuje.comunitario.web_services.dto.out.EventFilterResponseDTO;
import com.ong.empuje.comunitario.web_services.enums.DonationDistributionFilter;
import com.ong.empuje.comunitario.web_services.repository.EventJdbcRepository;
import com.ong.empuje.comunitario.web_services.service.EventService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventJdbcRepository eventRepository;

    public EventServiceImpl(EventJdbcRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventsDonationsResponseDTO> getParticipationEvents(
            String username,
            String startDate,
            String endDate,
            DonationDistributionFilter distribution
    ) {

        return eventRepository.findFilteredEvents(username, startDate, endDate, distribution).stream()
                .map(event -> {
                    EventsDonationsResponseDTO dto = new EventsDonationsResponseDTO();
                    dto.setName(event.getName());
                    dto.setDate(event.getDate());
                    dto.setDescription(event.getDescription());

                    if (event.getDonations() != null && !event.getDonations().isEmpty()) {
                        List<DonationDTO> donationDTOs = event.getDonations().stream()
                                .map(donation -> {
                                    DonationDTO donationDTO = new DonationDTO();
                                    donationDTO.setCategory(donation.getCategory());
                                    donationDTO.setDescription(donation.getDescription());
                                    donationDTO.setQuantity(donation.getQuantity());
                                    return donationDTO;
                                })
                                .collect(Collectors.toList());
                        dto.setDonations(donationDTOs);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<EventFilterResponseDTO> getFilters(String username){
       return eventRepository.getFilters(username);
    }

    public void saveFilter(EventFilterDTO eventFilter){
        eventRepository.saveFilter(eventFilter);
    }
}
