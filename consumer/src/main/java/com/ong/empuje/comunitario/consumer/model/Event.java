package com.ong.empuje.comunitario.consumer.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 200, nullable = false)
    private String name;

    @Column(length = 200, nullable = false)
    private String description;

    @Column(name="is_published")
    private boolean isPublished;

    @Column(name="event_datetime")
    private Date datetime;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    public Integer getId() {return id;}

    public String getName() {return name;}

    public String getDescription() {return description;}

    public boolean isPublished() {return isPublished;}

    public Date getDatetime() {return datetime;}

    public Organization getOrganization() {return organization;}

    public void setId(Integer id) {this.id = id;}

    public void setName(String name) {this.name = name;}

    public void setDescription(String description) {this.description = description;}

    public void setPublished(boolean published) {isPublished = published;}

    public void setDatetime(Date datetime) {this.datetime = datetime;}

    public void setOrganization(Organization organization) {this.organization = organization;}
}
