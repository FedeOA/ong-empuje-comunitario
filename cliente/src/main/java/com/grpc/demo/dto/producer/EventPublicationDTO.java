package com.grpc.demo.dto.producer;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventPublicationDTO(
        @JsonProperty("event_id")
        int eventId,
        @JsonProperty("organization_id")
        int organizationId,
        String name,
        String description,
        String datetime
) {
}
