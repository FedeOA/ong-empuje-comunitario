package com.grpc.demo.dto;

public record DonationDTO(
        String category,
        String description,
        int quantity
) {}
