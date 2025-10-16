package com.ong.empuje.comunitario.web_services.repository;

import com.ong.empuje.comunitario.web_services.dto.EventsDonationsResponseDTO;
import com.ong.empuje.comunitario.web_services.dto.in.EventFilterDTO;
import com.ong.empuje.comunitario.web_services.enums.DonationDistributionFilter;

import java.util.List;

public interface EventJdbcRepository {

    List<EventsDonationsResponseDTO> findFilteredEvents(String username, String startDate, String endDate, DonationDistributionFilter distribution);
    void saveFilter(EventFilterDTO eventFilter);
}
