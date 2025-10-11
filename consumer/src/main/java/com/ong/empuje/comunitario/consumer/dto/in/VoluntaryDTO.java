package com.ong.empuje.comunitario.consumer.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VoluntaryDTO(
        @JsonProperty("organization_id")
        int organizationId,
        @JsonProperty("voluntary_id")
        int voluntaryId,
        String name,
        @JsonProperty("last_name")
        String lastName,
        String phone,
        String email
) {
}
