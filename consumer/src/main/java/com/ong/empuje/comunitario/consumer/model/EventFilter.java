package com.ong.empuje.comunitario.consumer.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name="event_filter")
public class EventFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name")
    private String name;

    @Column(name="username")
    private String username;

    @Column(name="start_date")
    private Date startDate;

    @Column(name="end_date")
    private Date endDate;

    @Column(name="distribution")
    private String distribution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long getId() {return id;}

    public String getName() {return name;}

    public String getUsername() {return username;}

    public Date getStartDate() {return startDate;}

    public Date getEndDate() {return endDate;}

    public String getDistribution() {return distribution;}

    public User getUser() {return user;}

    public void setId(Long id) {this.id = id;}

    public void setName(String name) {this.name = name;}

    public void setUsername(String username) {this.username = username;}

    public void setStartDate(Date startDate) {this.startDate = startDate;}

    public void setEndDate(Date endDate) {this.endDate = endDate;}

    public void setDistribution(String distribution) {this.distribution = distribution;}

    public void setUser(User user) {this.user = user;}
}
