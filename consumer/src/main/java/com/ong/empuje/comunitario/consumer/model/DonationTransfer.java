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
@Table(name = "donation_transfer")
public class DonationTransfer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "transfer_id", nullable = false, unique = true)
    private int transferId;
    
    @Column(name = "organization_id", nullable = false)
    private int organizationId;
    
    @Column(name = "request_id")
    private Integer requestId; // Puede ser null si no está asociado a una solicitud
    
    @Column(name = "received", nullable = false)
    private boolean received = false;
    
    @Column(name = "processed", nullable = false)
    private boolean processed = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DonationTransferItem> items = new ArrayList<>();

    public DonationTransfer(){}

    //getters setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public int getTransferId() { return transferId; }
    public void setTransferId(int transferId) { this.transferId = transferId; }
    
    public int getOrganizationId() { return organizationId; }
    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }
    
    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }
    
    public boolean isReceived() { return received; }
    public void setReceived(boolean received) { this.received = received; }
    
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed;}
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    
    public List<DonationTransferItem> getItems() { return items; }
    public void setItems(List<DonationTransferItem> items) { this.items = items; }

    public void addItem(DonationTransferItem item) {
        items.add(item);
        item.setTransfer(this);
    }
}
