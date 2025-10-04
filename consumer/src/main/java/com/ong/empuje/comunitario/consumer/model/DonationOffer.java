package com.ong.empuje.comunitario.consumer.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "donation_offers")
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
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DonationOfferItem> items = new ArrayList<>();

    public DonationOffer(){}

    //getters setters
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
