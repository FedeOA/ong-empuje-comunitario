package com.grpc.demo.dto;

public record LoginResponseDTO(
        boolean isSuccess,
        String message,
        String role,
        String username,
        String jwt
) {
}

