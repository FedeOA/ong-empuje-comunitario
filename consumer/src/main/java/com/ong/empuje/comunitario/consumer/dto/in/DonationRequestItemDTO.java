// consumer\src\main\java\com\ong\empuje\comunitario\consumer\dto\in\DonationRequestItemDTO.java

package com.ong.empuje.comunitario.consumer.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DonationRequestItemDTO(
    @JsonProperty("category_id") Integer categoryId,
    @JsonProperty("description") String description
) {}