package com.grpc.demo.dto.out;

public record DonationResponseDTO(
        int id,
        String category,
        String description,
        int quantity,
        String username
) {
}
