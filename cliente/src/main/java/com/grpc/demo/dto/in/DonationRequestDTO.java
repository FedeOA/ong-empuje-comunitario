package com.grpc.demo.dto.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DonationRequestDTO {
    @JsonProperty("requestId")
    private Integer requestId;
    @JsonProperty("organizationId")
    private Integer organizationId;
    private List<DonationRequestItemDTO> items;

    // Getters/Setters
    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }
    public List<DonationRequestItemDTO> getItems() { return items; }
    public void setItems(List<DonationRequestItemDTO> items) { this.items = items; }
}