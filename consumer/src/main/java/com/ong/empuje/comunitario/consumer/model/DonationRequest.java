// consumer/src/main/java/com/ong/empuje/comunitario/consumer/model/DonationRequest.java

package com.ong.empuje.comunitario.consumer.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "donation_requests")
@IdClass(DonationRequestId.class)
public class DonationRequest {
    
    @Id
    @Column(name = "request_id")
    private Integer requestId;
    
    @Id
    @Column(name = "organization_id")
    private Integer organizationId;
    
    @Column(name = "is_deleted")
    private Boolean deleted = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DonationRequestItem> items = new ArrayList<>();
    
    // Constructors
    public DonationRequest() {}

    // Getters and Setters
    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }
    
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }
    
    public Boolean isDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<DonationRequestItem> getItems() { return items; }
    public void setItems(List<DonationRequestItem> items) { this.items = items; }
    
    public void addItem(DonationRequestItem item) {
        items.add(item);
        item.setRequest(this);
    }
}