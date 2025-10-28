package com.grpc.demo.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DonationEventDTO(
        @JsonProperty("category")
        int categoryId,
        String description,
        @JsonProperty("quantity")
        int quantityUsed) {
}
