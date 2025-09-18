package com.grpc.demo.dto.in;

public record DonationDTO(
        String category,
        String description,
        int quantity
) {}
