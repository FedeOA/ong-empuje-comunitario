package com.ong.empuje.comunitario.consumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "donation_requests")
@IdClass(DonationRequestId.class)
public class DonationRequest implements Serializable {
    
    @Id
    @Column(name = "request_id")
    @JsonProperty("requestId")
    private Integer requestId;
    
    @Id
    @Column(name = "organization_id")
    @JsonProperty("organizationId")
    private Integer organizationId;
    
    @Column(name = "is_deleted")
    @JsonProperty("deleted")
    private Boolean deleted = false;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "request_id", referencedColumnName = "request_id"),
        @JoinColumn(name = "organization_id", referencedColumnName = "organization_id")
    })
    @JsonProperty("items")
    private List<DonationRequestItem> items = new ArrayList<>();
    
    // Constructors
    public DonationRequest() {}

    // Getters and Setters
    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }
    
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }
    
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    
    public List<DonationRequestItem> getItems() { return items; }
    public void setItems(List<DonationRequestItem> items) { this.items = items; }
    
    public void addItem(DonationRequestItem item) {
        items.add(item);
    }
}