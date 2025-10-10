package com.ong.empuje.comunitario.consumer.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="donation_transfer_item")
public class DonationTransferItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "category_id", nullable=false)
    private int categoryId;

    @Column(name = "description", nullable=false)
    private String description;

    @Column(name = "quantity", nullable=false)
    private int quantity;

    @Column(name = "create_at",nullable=false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private DonationTransfer transfer;

    //constructor
    public DonationTransferItem(){
    }

    //getters setters
    public int getId() {
         return id; 
    }
    public void setId(int id) {
         this.id = id; 
    }
    
    public int getCategoryId() {
         return categoryId; 
    }
    public void setCategoryId(int categoryId) { 
        this.categoryId = categoryId; 
    }
    
    public String getDescription() { 
        return description; 
    }
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public int getQuantity() { 
        return quantity; 
    }
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
    
    public DonationTransfer getTransfer() { 
        return transfer; 
    }
    public void setTransfer(DonationTransfer transfer) { 
        this.transfer = transfer; 
    }

    
}
