package com.ong.empuje.comunitario.web_services.model;

import jakarta.persistence.*;

@Entity
@Table(name = "saved_filters")
public class SavedFilter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @Column(name = "deleted_status")
    private String deleted;

    public SavedFilter() {
        
    }

    public SavedFilter(Long id, String name, Category category, User user, String startDate, String endDate,
            String deleted) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deleted = deleted;
    }

    // Métodos calculados
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    
}