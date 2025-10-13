package com.ong.empuje.comunitario.web_services.dto;

public class FilterInput {
    private String name;
    private Integer categoryId;
    private String startDate;
    private String endDate;
    private String deletedStatus;

    public FilterInput() {

    }

    public FilterInput(String name, Integer categoryId, String startDate, String endDate, String deletedStatus) {
        this.name = name;
        this.categoryId = categoryId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deletedStatus = deletedStatus;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public String getDeletedStatus() {
        return deletedStatus;
    }
    public void setDeletedStatus(String deletedStatus) {
        this.deletedStatus = deletedStatus;
    }


}