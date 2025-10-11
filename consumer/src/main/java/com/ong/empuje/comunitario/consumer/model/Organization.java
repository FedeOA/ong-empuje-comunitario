package com.ong.empuje.comunitario.consumer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 200, nullable = false)
    private String name;

    public Integer getId() {return id;}

    public String getName() {return name;}

    public void setId(Integer id) {this.id = id;}

    public void setName(String name) {this.name = name;}
}
