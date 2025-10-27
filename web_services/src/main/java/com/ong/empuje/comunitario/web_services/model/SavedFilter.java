package com.ong.empuje.comunitario.web_services.model;

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
@Table(name = "saved_filters")
public class SavedFilter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "filter_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")  
    private Boolean filterDeleted = false; 

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted = false;

    public SavedFilter() {
    }

    public SavedFilter(Integer id, String name, Category category, User user, LocalDateTime startDate, LocalDateTime endDate, Boolean filterDeleted, Boolean deleted) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isDeleted = deleted;
        this.filterDeleted = filterDeleted;
    }

    public Integer getId() { 
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean deleted) {
        this.isDeleted = deleted;
    }

    public Integer getCategoryId() {
        return category != null ? category.getId() : null;
    }

    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    public Integer getUserId() {
        return user != null ? user.getId() : null;
    }

    public String getUsername() {
        return user != null ? user.getUsername() : null;
    }

    public Boolean getFilterDeleted() {
        return filterDeleted;
    }

    public void setFilterDeleted(Boolean filterDeleted) {
        this.filterDeleted = filterDeleted;
    }
}