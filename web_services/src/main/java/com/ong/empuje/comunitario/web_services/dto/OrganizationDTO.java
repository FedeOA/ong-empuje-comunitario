package com.ong.empuje.comunitario.web_services.dto;


public class OrganizationDTO {
    
    private int id;
    private String name;
    private String address;
    private String phone;

    public OrganizationDTO(){}

    public OrganizationDTO(int id, String name, String address, String phone){
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public int getId() { return id; }
    public void setId(int value) { this.id = value; }
    
    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
    
    public String getAddress() { return address; }
    public void setAddress(String value) { this.address = value; }
    
    public String getPhone() { return phone; }
    public void setPhone(String value) { this.phone = value; }
}
