package com.ong.empuje.comunitario.consumer.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrganizationDTO {
    @JsonProperty("organization_id")
    private Integer organizationId;

    @JsonProperty("name")
    private String name;

    public OrganizationDTO() {}

    public OrganizationDTO(Integer organizationId, String name) {
        this.organizationId = organizationId;
        this.name = name;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}