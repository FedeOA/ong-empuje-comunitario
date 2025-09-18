package com.grpc.demo.dto.out;

public record ResponseDTO(
        boolean isSuccess,
        String message
) {}
