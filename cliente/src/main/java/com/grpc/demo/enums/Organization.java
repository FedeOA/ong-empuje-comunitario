package com.grpc.demo.enums;

public enum Organization {

    ONG_EMPUJE_COMUNITARIO(1,"ONG EMPUJE COMUNITARIO"),
    ONG_SOMOS_MAS(2,"ONG SOMOS MAS");

    private final int id;
    private final String name;

    Organization(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {return id;}

    public String getName() {return name;}
}
