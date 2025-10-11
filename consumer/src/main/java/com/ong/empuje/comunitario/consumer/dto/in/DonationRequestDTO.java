package com.ong.empuje.comunitario.consumer.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DonationRequestDTO {
    @JsonProperty("requestId")
    private Integer requestId;

    @JsonProperty("organizationId")
    private Integer organizationId;

    @JsonProperty("items")
    private List<DonationRequestItemDTO> items;

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public List<DonationRequestItemDTO> getItems() {
        return items;
    }

    public void setItems(List<DonationRequestItemDTO> items) {
        this.items = items;
    }
}