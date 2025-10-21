package com.ong.empuje.comunitario.web_services.dto.in;

import java.time.LocalDateTime;

public class DonationExcelRequestDTO {
    private Integer categoryId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean deleted;

    public DonationExcelRequestDTO(){}

    public DonationExcelRequestDTO(Integer categoryId, LocalDateTime startDate, LocalDateTime endDate, Boolean deleted){
        this.categoryId = categoryId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deleted = deleted;
    }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
}
