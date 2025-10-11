package com.grpc.demo.dto.producer;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventVoluntaryDTO(
       @JsonProperty("remote_id")
       Integer remoteId,
       @JsonProperty("origin_organization_id")
       Integer originOrganizationId,
       VoluntaryDTO voluntary
) {
}
