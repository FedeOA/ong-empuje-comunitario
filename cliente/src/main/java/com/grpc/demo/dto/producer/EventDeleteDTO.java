package com.grpc.demo.dto.producer;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventDeleteDTO(
        @JsonProperty("event_id")
        int eventId,
        @JsonProperty("organization_id")
        int organizationId
) {
}
