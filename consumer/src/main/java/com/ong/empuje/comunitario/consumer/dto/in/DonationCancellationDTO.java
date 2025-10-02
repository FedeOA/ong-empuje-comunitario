// consumer\src\main\java\com\ong\empuje\comunitario\consumer\dto\in\DonationCancellationDTO.java

package com.ong.empuje.comunitario.consumer.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DonationCancellationDTO(
        @JsonProperty("request_id")
        Integer requestId,
        @JsonProperty("organization_id")
        Integer organizationId
) {
}