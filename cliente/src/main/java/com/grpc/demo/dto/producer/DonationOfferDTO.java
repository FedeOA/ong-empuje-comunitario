package com.grpc.demo.dto.producer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DonationOfferDTO (
    @JsonProperty("offer_id") int offerId,
    @JsonProperty("organization_id") int organizationId,
    @JsonProperty("items") List<DonationOfferItemDTO> items
) {
}
