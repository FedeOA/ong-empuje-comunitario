package com.grpc.demo.dto.out;

public record LoginResponseDTO(
        boolean isSuccess,
        String message,
        String role,
        String username,
        String token
) {
}

