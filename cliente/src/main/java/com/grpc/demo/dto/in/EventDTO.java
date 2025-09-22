package com.grpc.demo.dto.in;

public record EventDTO(
        String name,
        String description,
        String datetime
) {
}

