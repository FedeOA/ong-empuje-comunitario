package com.grpc.demo.enums;

public enum Topic {

    EVENTOS_SOLIDARIOS("eventos-solidarios"),
    ADHESION_EVENTO("adhesion-evento"),
    BAJA_EVENTO_SOLIDARIO("baja-evento-solidario");

    private final String name;

    Topic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
