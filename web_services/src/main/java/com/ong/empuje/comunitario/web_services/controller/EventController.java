package com.ong.empuje.comunitario.web_services.controller;

import com.ong.empuje.comunitario.web_services.dto.EventsDonationsResponseDTO;
import com.ong.empuje.comunitario.web_services.enums.DonationDistributionFilter;
import com.ong.empuje.comunitario.web_services.service.EventService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.Collection;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedUsername = authentication.getName();
        Collection<? extends GrantedAuthority> roles = authentication.getAuthorities();

        boolean isPrivileged = roles.stream().anyMatch(role ->
                role.getAuthority().equals("ROLE_PRESIDENTE") || role.getAuthority().equals("ROLE_COORDINADOR"));

        if (!isPrivileged && !loggedUsername.equals(username)) {
            throw new AccessDeniedException("No tenés permiso para consultar datos de otro usuario");
        }

        return eventService.getParticipationEvents(username, startDate, endDate, distribution);
    }



}
