package com.ong.empuje.comunitario.consumer.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "user_events")
public class UserEvents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "registration_date")
    private Date registrationDate;

    public Integer getId() {return id;}

    public User getUser() {return user;}

    public Event getEvent() {return event;}

    public Date getRegistrationDate() {return registrationDate;}

    public void setId(Integer id) {this.id = id;}

    public void setUser(User user) {this.user = user;}

    public void setEvent(Event event) {this.event = event;}

    public void setRegistrationDate(Date registrationDate) {this.registrationDate = registrationDate;}
}
