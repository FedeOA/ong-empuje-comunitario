package com.ong.empuje.comunitario.consumer.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "voluntary_events")
public class VoluntaryEvents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "voluntary_id", nullable = false)
    private Voluntary voluntary;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "registration_date")
    private Date registrationDate;

    public Integer getId() {return id;}

    public Voluntary getVoluntary() {return voluntary;}

    public Event getEvent() {return event;}

    public Date getRegitrationDate() {return registrationDate;}

    public void setId(Integer id) {this.id = id;}

    public void setVoluntary(Voluntary voluntary) {this.voluntary = voluntary;}

    public void setEvent(Event event) {this.event = event;}

    public void setRegistrationDate(Date regitrationDate) {this.registrationDate = regitrationDate;}
}
