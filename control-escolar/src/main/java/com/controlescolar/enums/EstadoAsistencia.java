package com.controlescolar.enums;

public enum EstadoAsistencia {
    PRESENTE("Presente"),
    AUSENTE("Ausente"),
    TARDANZA("Tardanza"),
    JUSTIFICADO("Justificado");

    private final String descripcion;

    EstadoAsistencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}