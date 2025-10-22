package com.ong.empuje.comunitario.consumer.dto.in;

public class CancelDonationOfferPayloadDTO {
    private int offerId;
    private int organizationId;

    public int getOfferId() { return offerId; }
    public void setOfferId(int offerId) { this.offerId = offerId; }
    public int getOrganizationId() { return organizationId; }
    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }
}