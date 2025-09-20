package com.grpc.demo.dto.out;

import java.util.Date;

public record EventResponseDTO(
        int id,
        String name,
        String description,
        String datetime
) {
}
