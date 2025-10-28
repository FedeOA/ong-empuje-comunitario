package com.ong.empuje.comunitario.consumer.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DonationTransferItemDTO(
    @JsonProperty("item_id") Integer itemId,
    @JsonProperty("category_id") Integer categoryId,
    @JsonProperty("description") String description,
    @JsonProperty("quantity") Integer quantity
) {
    public DonationTransferItemDTO(Integer itemId, Integer categoryId, String description, Integer quantity) {
        this.itemId = itemId;
        this.categoryId = categoryId;
        this.description = description;
        this.quantity = quantity;
    }
}
