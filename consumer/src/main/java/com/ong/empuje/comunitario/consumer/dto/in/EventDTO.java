package com.ong.empuje.comunitario.consumer.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventDTO(
        @JsonProperty("event_id")
        Integer eventId,
        String name,
        String description,
        String datetime,
        @JsonProperty("organization_id")
        String organizationId
) {
}
