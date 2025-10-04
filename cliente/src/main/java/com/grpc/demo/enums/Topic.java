package com.grpc.demo.enums;

public enum Topic {

    EVENTOS_SOLIDARIOS("eventos-solidarios"),
    ADHESION_EVENTO("adhesion-evento"),
    BAJA_EVENTO_SOLIDARIO("baja-evento-solidario"),

    TRANSFERENCIA_DONACIONES("transferencia_donaciones"),
    OFERTA_DONACIONES("oferta_donaciones");


    private final String name;

    Topic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
