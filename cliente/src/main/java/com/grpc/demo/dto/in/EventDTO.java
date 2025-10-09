package com.grpc.demo.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventDTO(
        String name,
        String description,
        String datetime,
        @JsonProperty("is_published")
        boolean isPublished
) {
}

