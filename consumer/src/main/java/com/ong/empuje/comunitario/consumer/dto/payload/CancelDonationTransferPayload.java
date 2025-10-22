package com.ong.empuje.comunitario.consumer.dto.payload;

public class CancelDonationTransferPayload {
    private int transferId;
    private int organizationId;

    public int getTransferId() { return transferId; }
    public void setTransferId(int transferId) { this.transferId = transferId; }
    public int getOrganizationId() { return organizationId; }
    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }
}