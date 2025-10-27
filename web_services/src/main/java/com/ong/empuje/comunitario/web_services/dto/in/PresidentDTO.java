package com.ong.empuje.comunitario.web_services.dto.in;


public class PresidentDTO {
    
    private int id;
    private String name;
    private String address;
    private String phone;
    private int organizationId;

    public PresidentDTO(){}

    public PresidentDTO(int id, String name, String address, String phone, int organizationId){
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.organizationId = organizationId;
    }

    public int getId() { return id; }
    public void setId(int value) { this.id = value; }
    
    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
    
    public String getAddress() { return address; }
    public void setAddress(String value) { this.address = value; }
    
    public String getPhone() { return phone; }
    public void setPhone(String value) { this.phone = value; }
    
    public int getOrganizationId() { return organizationId; }
    public void setOrganizationId(int value) { this.organizationId = value; }
}
