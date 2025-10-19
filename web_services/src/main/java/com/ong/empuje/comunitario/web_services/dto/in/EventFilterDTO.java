package com.ong.empuje.comunitario.web_services.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventFilterDTO(
        String name,
        String searchUsername,
        String distribution,
        String startDate,
        String endDate,
        String username
) {
}
