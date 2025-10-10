package com.ong.empuje.comunitario.consumer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public Integer getId() {return id;}

    public void setId(Integer id) {this.id = id;}
}
