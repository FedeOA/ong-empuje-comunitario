package com.ong.empuje.comunitario.web_services.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventFilterDTO(
        String name,
        String username,
        String distribution,
        @JsonProperty("start_date")
        String startDate,
        @JsonProperty("end_date")
        String endDate,
        @JsonProperty("search_username")
        String searchUsername
) {
}
