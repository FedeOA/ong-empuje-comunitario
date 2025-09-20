package com.grpc.demo.dto.in;

import java.util.Date;

public record EventDTO(
        String name,
        String description,
        Date datetime
) {
}

