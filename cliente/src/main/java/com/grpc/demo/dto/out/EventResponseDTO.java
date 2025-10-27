package com.grpc.demo.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EventResponseDTO(
        int id,
        String name,
        String description,
        String datetime,
        @JsonProperty("is_published")
        boolean isPublished,
        List<String> users,
        List<DonationResponseDTO> donations
) {
}
