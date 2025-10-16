package com.ong.empuje.comunitario.web_services.controller;

import com.ong.empuje.comunitario.web_services.dto.EventsDonationsResponseDTO;
import com.ong.empuje.comunitario.web_services.enums.DonationDistributionFilter;
import com.ong.empuje.comunitario.web_services.service.EventService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @QueryMapping
    public List<EventsDonationsResponseDTO> participationEvents(
            @Argument String username,
            @Argument String startDate,
            @Argument String endDate,
            @Argument DonationDistributionFilter distribution
    ) {
        return eventService.getParticipationEvents(username, startDate, endDate, distribution);
    }

}
