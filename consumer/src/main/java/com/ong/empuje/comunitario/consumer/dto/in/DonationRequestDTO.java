// consumer\src\main\java\com\ong\empuje\comunitario\consumer\dto\in\DonationRequestDTO.java

package com.ong.empuje.comunitario.consumer.dto.in;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DonationRequestDTO(
    @JsonProperty("request_id") Integer requestId,
    @JsonProperty("organization_id") Integer organizationId,
    @JsonProperty("items") List<DonationRequestItemDTO> items
) {}