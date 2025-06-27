package com.controlescolar.enums;

/**
 * Enum que define los diferentes tipos de pago en el sistema escolar
 */
public enum TipoPago {
    COLEGIATURA("Colegiatura", "Pago mensual de colegiatura", "#3498db"),
    INSCRIPCION("Inscripción", "Pago de inscripción anual", "#e74c3c"),
    REINSCRIPCION("Reinscripción", "Pago de reinscripción", "#f39c12"),
    EXAMEN("Examen", "Pago por examen extraordinario", "#9b59b6"),
    LABORATORIO("Laboratorio", "Pago por uso de laboratorio", "#1abc9c"),
    BIBLIOTECA("Biblioteca", "Pago por servicios de biblioteca", "#34495e"),
    MATERIAL("Material", "Pago por material didáctico", "#e67e22"),
    UNIFORME("Uniforme", "Pago por uniforme escolar", "#95a5a6"),
    SEGURO("Seguro", "Pago de seguro estudiantil", "#16a085"),
    CREDENCIAL("Credencial", "Pago por credencial estudiantil", "#8e44ad"),
    DIPLOMA("Diploma", "Pago por expedición de diploma", "#f1c40f"),
    CERTIFICADO("Certificado", "Pago por certificado de estudios", "#d35400"),
    TRANSPORTE("Transporte", "Pago por servicio de transporte", "#27ae60"),
    COMEDOR("Comedor", "Pago por servicio de comedor", "#2980b9"),
    ACTIVIDADES("Actividades", "Pago por actividades extracurriculares", "#c0392b"),
    OTROS("Otros", "Otros tipos de pago", "#7f8c8d");

    private final String nombre;
    private final String descripcion;
    private final String colorHex;

    /**
     * Constructor del enum TipoPago
     * @param nombre Nombre del tipo de pago
     * @param descripcion Descripción del tipo de pago
     * @param colorHex Color hexadecimal para mostrar en la interfaz
     */
    TipoPago(String nombre, String descripcion, String colorHex) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.colorHex = colorHex;
    }

    /**
     * Obtiene el nombre del tipo de pago
     * @return String con el nombre del tipo
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene la descripción del tipo de pago
     * @return String con la descripción del tipo
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Obtiene el color hexadecimal del tipo
     * @return String con el color en formato hexadecimal
     */
    public String getColorHex() {
        return colorHex;
    }

    /**
     * Verifica si es un tipo de pago recurrente (mensual)
     * @return true si es recurrente
     */
    public boolean esRecurrente() {
        return this == COLEGIATURA || this == TRANSPORTE || this == COMEDOR;
    }

    /**
     * Verifica si es un pago único (anual o por evento)
     * @return true si es pago único
     */
    public boolean esPagoUnico() {
        return this == INSCRIPCION || this == REINSCRIPCION || this == DIPLOMA || 
               this == CERTIFICADO || this == CREDENCIAL;
    }

    /**
     * Convierte el enum a string para mostrar en interfaces
     * @return String con el nombre del tipo
     */
    @Override
    public String toString() {
        return nombre;
    }

    /**
     * Obtiene un tipo por su nombre
     * @param nombre Nombre del tipo a buscar
     * @return TipoPago encontrado o null si no existe
     */
    public static TipoPago fromNombre(String nombre) {
        for (TipoPago tipo : values()) {
            if (tipo.getNombre().equalsIgnoreCase(nombre)) {
                return tipo;
            }
        }
        return null;
    }

    /**
     * Obtiene los tipos de pago más comunes
     * @return Array con los tipos más utilizados
     */
    public static TipoPago[] getTiposComunes() {
        return new TipoPago[]{COLEGIATURA, INSCRIPCION, REINSCRIPCION, EXAMEN, MATERIAL};
    }
}