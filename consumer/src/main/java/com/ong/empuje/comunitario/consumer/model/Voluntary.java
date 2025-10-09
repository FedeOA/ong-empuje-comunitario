package com.ong.empuje.comunitario.consumer.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "voluntaries")
public class Voluntary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="organization_id")
    private Integer organizationId;

    @Column(name="voluntary_id")
    private Integer voluntaryId;

    @Column(length = 200, nullable = false)
    private String name;

    @Column(name = "last_name", length = 200, nullable = false)
    private String lastName;

    @Column(length = 200, nullable = false)
    private String phone;

    @Column(length = 200, nullable = false)
    private String email;

    @OneToMany(mappedBy = "voluntary")
    private List<VoluntaryEvents> voluntaryEvents;

    public Integer getId() {return id;}

    public Integer getOrganizationId() {return organizationId;}

    public String getName() {return name;}

    public String getLastName() {return lastName;}

    public String getPhone() {return phone;}

    public String getEmail() {return email;}

    public Integer getVoluntaryId() {return voluntaryId;}

    public List<VoluntaryEvents> getVoluntaryEvents() {return voluntaryEvents;}

    public void setId(Integer id) {this.id = id;}

    public void setOrganizationId(Integer organizationId) {this.organizationId = organizationId;}

    public void setName(String name) {this.name = name;}

    public void setLastName(String lastName) {this.lastName = lastName;}

    public void setPhone(String phone) {this.phone = phone;}

    public void setEmail(String email) {this.email = email;}

    public void setVoluntaryId(Integer voluntaryId) {this.voluntaryId = voluntaryId;}

    public void setVoluntaryEvents(List<VoluntaryEvents> voluntaryEvents) {this.voluntaryEvents = voluntaryEvents;}
}
