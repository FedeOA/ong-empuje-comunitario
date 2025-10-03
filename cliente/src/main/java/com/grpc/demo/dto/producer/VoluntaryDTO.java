package com.grpc.demo.dto.producer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grpc.demo.enums.Organization;

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

    public VoluntaryDTO {
            organizationId = Organization.ONG_EMPUJE_COMUNITARIO.getId();
    }
}
