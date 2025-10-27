package com.grpc.demo.enums;

public enum Topic {

    EVENTOS_SOLIDARIOS("eventos-solidarios"),
    ADHESION_EVENTO("adhesion-evento"),
    BAJA_EVENTO_SOLIDARIO("baja-evento-solidario"),
    SOLICITUD_DONACIONES("solicitud_donaciones"),
    BAJA_SOLICITUD_DONACIONES("baja_solicitud_donaciones"),
    TRANSFERENCIA_DONACIONES("transferencia-donaciones"),
    OFERTA_DONACIONES("oferta-donaciones");

    private final String name;

    Topic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
