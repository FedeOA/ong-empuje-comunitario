package com.ong.empuje.comunitario.consumer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "donation_request_items")
public class DonationRequestItem implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Integer id;
    
    @Column(name = "request_id", insertable = false, updatable = false)
    @JsonProperty("requestId")
    private Integer requestId;
    
    @Column(name = "organization_id", insertable = false, updatable = false)
    @JsonProperty("organizationId")
    private Integer organizationId;
    
    @Column(name = "category_id")
    @JsonProperty("categoryId")
    private Integer categoryId;
    
    @Column(name = "description")
    @JsonProperty("description")
    private String description;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "organization_id", referencedColumnName = "organization_id"),
        @JoinColumn(name = "request_id", referencedColumnName = "request_id")
    })
    @JsonBackReference
    private DonationRequest donationRequest;
    
    public DonationRequestItem() {}
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }
    
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }
    
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DonationRequest getDonationRequest() { return donationRequest; }
    public void setDonationRequest(DonationRequest donationRequest) { this.donationRequest = donationRequest; }
}