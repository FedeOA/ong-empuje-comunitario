package com.ong.empuje.comunitario.consumer.dto.in;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class DonationOfferPayloadDTO {
    private int offerId;
    private int organizationId;
    @JsonFormat(pattern = "yy:MM:dd") // Expect YY:MM:DD format from frontend
    private String expiresAt;
    private List<DonationOfferItemPayloadDTO> items;

    public int getOfferId() { return offerId; }
    public void setOfferId(int offerId) { this.offerId = offerId; }
    public int getOrganizationId() { return organizationId; }
    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public List<DonationOfferItemPayloadDTO> getItems() { return items; }
    public void setItems(List<DonationOfferItemPayloadDTO> items) { this.items = items; }

    public LocalDateTime getExpiresAtAsLocalDateTime() {
        if (expiresAt == null || expiresAt.isEmpty()) {
            return null;
        }
        try {
            // Parse YY:MM:DD format and prepend century (assuming 20XX)
            String[] parts = expiresAt.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid expiresAt format: " + expiresAt);
            }
            String year = "20" + parts[0]; // Convert YY to YYYY (e.g., 25 -> 2025)
            String formattedDate = String.format("%s-%s-%sT00:00:00", year, parts[1], parts[2]);
            return LocalDateTime.parse(formattedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid expiresAt format: " + expiresAt, e);
        }
    }
}