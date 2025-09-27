package com.grpc.demo.dto.out;

import java.util.List;

public record EventResponseDTO(
        int id,
        String name,
        String description,
        String datetime,
        List<String> users
) {
}
