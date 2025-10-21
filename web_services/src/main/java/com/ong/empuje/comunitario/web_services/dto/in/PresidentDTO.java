// web_services/src/main/java/com/ong/empuje/comunitario/web_services/dto/in/PresidentDTO.java
package com.ong.empuje.comunitario.web_services.dto.in;

public class PresidentDTO {
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String role;
    private int organizationId;

    public PresidentDTO() {}

    public PresidentDTO(int id, String username, String firstName, String lastName, String phone, String email, String role, int organizationId) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.organizationId = organizationId;
    }

    public int getId() { return id; }
    public void setId(int value) { this.id = value; }
    public String getUsername() { return username; }
    public void setUsername(String value) { this.username = value; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String value) { this.firstName = value; }
    public String getLastName() { return lastName; }
    public void setLastName(String value) { this.lastName = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { this.phone = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { this.email = value; }
    public String getRole() { return role; }
    public void setRole(String value) { this.role = value; }
    public int getOrganizationId() { return organizationId; }
    public void setOrganizationId(int value) { this.organizationId = value; }
}