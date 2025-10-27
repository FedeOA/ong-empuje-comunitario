package com.ong.empuje.comunitario.web_services.dto;

import com.ong.empuje.comunitario.web_services.model.Donation;
import java.util.List;

public class DonationReportGroup {
    private Integer categoryId;
    private String categoryName;
    private Boolean deleted;
    private Integer totalQuantity;
    private List<Donation> donations;
    
    // Constructor vacío requerido por GraphQL
    public DonationReportGroup() {}

    

    public DonationReportGroup(Integer categoryId, String categoryName, Boolean deleted, Integer totalQuantity,
            List<Donation> donations) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.deleted = deleted;
        this.totalQuantity = totalQuantity;
        this.donations = donations;
    }



    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public List<Donation> getDonations() {
        return donations;
    }

    public void setDonations(List<Donation> donations) {
        this.donations = donations;
    }
    

    
}