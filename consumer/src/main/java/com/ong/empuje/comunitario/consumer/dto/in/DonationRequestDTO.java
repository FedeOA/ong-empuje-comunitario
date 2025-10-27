package com.ong.empuje.comunitario.consumer.dto.in;

import java.util.List;

public class DonationRequestDTO {
    private Integer requestId;
    private Integer organizationId;
    private Boolean deleted;
    private List<DonationRequestItemDTO> items;

    // Constructors
    public DonationRequestDTO() {}

    // Getters and Setters
    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public List<DonationRequestItemDTO> getItems() { return items; }
    public void setItems(List<DonationRequestItemDTO> items) { this.items = items; }
}