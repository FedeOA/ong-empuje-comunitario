package com.ong.empuje.comunitario.web_services.service;

import com.ong.empuje.comunitario.web_services.dto.EventsDonationsResponseDTO;
import com.ong.empuje.comunitario.web_services.enums.DonationDistributionFilter;

import java.util.List;

public interface EventService {

    List<EventsDonationsResponseDTO> getParticipationEvents(
            String username,
            String startDate,
            String endDate,
            DonationDistributionFilter distribution
    );
}
