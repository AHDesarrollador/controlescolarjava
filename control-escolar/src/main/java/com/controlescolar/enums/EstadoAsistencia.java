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

    /**
     * Obtiene un estado por su descripción
     * @param descripcion Descripción del estado a buscar
     * @return EstadoAsistencia encontrado o null si no existe
     */
    public static EstadoAsistencia fromDescripcion(String descripcion) {
        for (EstadoAsistencia estado : values()) {
            if (estado.getDescripcion().equalsIgnoreCase(descripcion)) {
                return estado;
            }
        }
        return null;
    }
}