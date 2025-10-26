package com.ong.empuje.comunitario.web_services.dto;

import java.time.LocalDateTime;

public class FilterInput {
    private String name;
    private Integer categoryId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean deleted;
    private Boolean filterDeleted;
    private String username;

    public FilterInput() {
    }

    public FilterInput(String name, Integer categoryId, LocalDateTime startDate, LocalDateTime endDate, Boolean filterDeleted, Boolean deleted, String username) {
        this.name = name;
        this.categoryId = categoryId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.filterDeleted = filterDeleted;
        this.deleted = deleted;
        this.username = username;
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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getFilterDeleted() {
        return filterDeleted;
    }

    public void setFilterDeleted(Boolean filterDeleted) {
        this.filterDeleted = filterDeleted;
    }
}