package com.ong.empuje.comunitario.consumer.dto.in;

public class DonationRequestItemDTO {
    private Integer id;
    private Integer categoryId;
    private String description;

    // Constructors
    public DonationRequestItemDTO() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}