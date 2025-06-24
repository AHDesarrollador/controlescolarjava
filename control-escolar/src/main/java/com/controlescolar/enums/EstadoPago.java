package com.controlescolar.enums;

/**
 * Enum que define los diferentes estados de pago de colegiatura
 */
public enum EstadoPago {
    PENDIENTE("Pendiente", "Pago no realizado", "#FF6B6B", false),
    PAGADO("Pagado", "Pago completado", "#4ECDC4", true),
    PARCIAL("Parcial", "Pago parcialmente completado", "#FFE66D", false),
    VENCIDO("Vencido", "Pago fuera de fecha límite", "#FF4757", false),
    CANCELADO("Cancelado", "Pago cancelado", "#747D8C", false),
    REEMBOLSADO("Reembolsado", "Pago reembolsado", "#A4B0BE", false),
    BECADO("Becado", "Alumno con beca", "#5F27CD", true),
    CONDONADO("Condonado", "Pago condonado por la institución", "#00D2D3", true);

    private final String nombre;
    private final String descripcion;
    private final String colorHex;
    private final boolean completado;

    /**
     * Constructor del enum EstadoPago
     * @param nombre Nombre del estado
     * @param descripcion Descripción del estado
     * @param colorHex Color hexadecimal para mostrar en la interfaz
     * @param completado Indica si el pago está completado
     */
    EstadoPago(String nombre, String descripcion, String colorHex, boolean completado) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.colorHex = colorHex;
        this.completado = completado;
    }

    /**
     * Obtiene el nombre del estado
     * @return String con el nombre del estado
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene la descripción del estado
     * @return String con la descripción del estado
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Obtiene el color hexadecimal del estado
     * @return String con el color en formato hexadecimal
     */
    public String getColorHex() {
        return colorHex;
    }

    /**
     * Verifica si el pago está completado
     * @return true si el pago está completado
     */
    public boolean isCompletado() {
        return completado;
    }

    /**
     * Verifica si el estado requiere acción
     * @return true si requiere acción
     */
    public boolean requiereAccion() {
        return this == PENDIENTE || this == PARCIAL || this == VENCIDO;
    }

    /**
     * Verifica si es un estado final (no puede cambiar)
     * @return true si es estado final
     */
    public boolean esFinal() {
        return this == CANCELADO || this == REEMBOLSADO || this == CONDONADO;
    }

    /**
     * Obtiene los estados válidos para transición desde el estado actual
     * @return Array de estados válidos para transición
     */
    public EstadoPago[] getEstadosValidos() {
        switch (this) {
            case PENDIENTE:
                return new EstadoPago[]{PAGADO, PARCIAL, VENCIDO, CANCELADO, BECADO, CONDONADO};
            case PARCIAL:
                return new EstadoPago[]{PAGADO, VENCIDO, CANCELADO};
            case VENCIDO:
                return new EstadoPago[]{PAGADO, PARCIAL, CANCELADO, CONDONADO};
            case PAGADO:
                return new EstadoPago[]{REEMBOLSADO};
            default:
                return new EstadoPago[]{};
        }
    }

    /**
     * Convierte el enum a string para mostrar en interfaces
     * @return String con el nombre del estado
     */
    @Override
    public String toString() {
        return nombre;
    }

    /**
     * Obtiene un estado por su nombre
     * @param nombre Nombre del estado a buscar
     * @return EstadoPago encontrado o null si no existe
     */
    public static EstadoPago fromNombre(String nombre) {
        for (EstadoPago estado : values()) {
            if (estado.getNombre().equalsIgnoreCase(nombre)) {
                return estado;
            }
        }
        return null;
    }
}