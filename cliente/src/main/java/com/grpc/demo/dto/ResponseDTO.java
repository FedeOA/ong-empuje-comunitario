package com.grpc.demo.dto;

public record ResponseDTO(
        boolean isSuccess,
        String message
) {}
