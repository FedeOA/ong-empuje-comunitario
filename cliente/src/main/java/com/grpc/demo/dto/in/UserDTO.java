package com.grpc.demo.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDTO(
        String username,
        @JsonProperty("first_name")
        String firstName,
        @JsonProperty("last_name")
        String lastName,
        String phone,
        String email,
        String role
) {

}
