package com.ong.empuje.comunitario.consumer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "donation_offers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DonationOffer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "offer_id", nullable = false, unique = true)
    private int offerId;
    
    @Column(name = "organization_id", nullable = false)
    private int organizationId;
    
    @Column(name = "available", nullable = false)
    private boolean available = true;
    
    @Column(name = "created_at", nullable = false)
    @JsonFormat(pattern = "yy:MM:dd")
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    @JsonFormat(pattern = "yy:MM:dd")
    private LocalDateTime expiresAt;
    
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DonationOfferItem> items = new ArrayList<>();

    public DonationOffer() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public int getOfferId() { return offerId; }
    public void setOfferId(int offerId) { this.offerId = offerId; }
    public int getOrganizationId() { return organizationId; }
    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public List<DonationOfferItem> getItems() { return items; }
    public void setItems(List<DonationOfferItem> items) { this.items = items; }

    public void addItem(DonationOfferItem item) {
        items.add(item);
        item.setOffer(this);
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}