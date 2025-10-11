package com.grpc.demo.dto.producer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DonationTransferDTO(
    @JsonProperty("request_id") int requestId,
    @JsonProperty("organization_id") int organizationId,
    @JsonProperty("items") List<DonationTransferItemDTO> items
) {
}
