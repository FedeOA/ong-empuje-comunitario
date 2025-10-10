package com.grpc.demo.enums;

public enum Topic {

    EVENTOS_SOLIDARIOS("eventos-solidarios"),
    ADHESION_EVENTO("adhesion-evento"),
    BAJA_EVENTO_SOLIDARIO("baja-evento-solidario"),
    ALTA_SOLICITUD_DONACION("alta-solicitud-donacion"),
    BAJA_SOLICITUD_DONACION("baja-solicitud-donacion");

    private final String name;

    Topic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
