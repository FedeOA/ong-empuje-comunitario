package com.grpc.demo.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponseDTO (
        int id,
        String username,
        @JsonProperty("first_name")
        String firstName,
        @JsonProperty("last_name")
        String lastName,
        String phone,
        String email,
        int role,
        @JsonProperty("is_active")
        boolean isActive
){
}
