package com.ong.empuje.comunitario.consumer.dto.in;

public class DonationOfferItemPayloadDTO {
    private int categoryId;
    private String description;
    private int quantity;

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
