package com.grpc.demo.dto.producer;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DonationOfferItemDTO(
    @JsonProperty("category_id") int categoryId,
    @JsonProperty("description") String description,
    @JsonProperty("quantity") int quantity
){   
}
