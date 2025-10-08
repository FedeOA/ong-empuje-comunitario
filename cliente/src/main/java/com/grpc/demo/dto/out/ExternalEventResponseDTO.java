package com.grpc.demo.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ExternalEventResponseDTO(
        int id,
        String name,
        String description,
        String datetime,
        @JsonProperty("is_published")
        boolean isPublished,
        @JsonProperty("remote_id")
        int remoteId,
        @JsonProperty("organization_id")
        int organizationId,
        List<String> users
) {
}
