package com.ong.empuje.comunitario.web_services.dto.in;

public class OrganizationDTO {
    private int id;
    private String name;

    public OrganizationDTO() {}

    public OrganizationDTO(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int value) { this.id = value; }
    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
}