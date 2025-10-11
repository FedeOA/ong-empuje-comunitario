package com.ong.empuje.comunitario.consumer.dto.in;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DonationOfferDTO (
    @JsonProperty("offer_id") int offerId,
    @JsonProperty("organization_id") int organizationId,
    @JsonProperty("items") List<DonationTransferItemDTO> items
) {
}
