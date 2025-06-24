package com.controlescolar.enums;

/**
 * Enum que define los diferentes tipos de calificación en el sistema educativo
 */
public enum TipoCalificacion {
    EXAMEN_PARCIAL("Examen Parcial", "EP", 25.0, true, "Evaluación parcial del período"),
    EXAMEN_FINAL("Examen Final", "EF", 40.0, true, "Evaluación final del curso"),
    TAREA("Tarea", "T", 10.0, false, "Actividad para realizar en casa"),
    PROYECTO("Proyecto", "P", 15.0, true, "Trabajo de investigación o práctica"),
    PARTICIPACION("Participación", "PART", 5.0, false, "Participación en clase"),
    PRACTICA("Práctica", "PR", 10.0, false, "Ejercicio práctico"),
    ENSAYO("Ensayo", "E", 15.0, true, "Trabajo escrito de análisis"),
    PRESENTACION("Presentación", "PRES", 12.0, true, "Exposición oral"),
    QUIZ("Quiz", "Q", 8.0, false, "Evaluación rápida"),
    LABORATORIO("Laboratorio", "LAB", 20.0, true, "Práctica de laboratorio"),
    EXTRAORDINARIO("Extraordinario", "EXT", 100.0, true, "Examen extraordinario"),
    TITULO("Título", "TIT", 100.0, true, "Examen de titulación");

    private final String nombre;
    private final String abreviacion;
    private final double pesoDefault;
    private final boolean requiereAprobacion;
    private final String descripcion;

    /**
     * Constructor del enum TipoCalificacion
     * @param nombre Nombre completo del tipo de calificación
     * @param abreviacion Abreviación del tipo
     * @param pesoDefault Peso porcentual por defecto
     * @param requiereAprobacion Si requiere calificación mínima aprobatoria
     * @param descripcion Descripción del tipo de calificación
     */
    TipoCalificacion(String nombre, String abreviacion, double pesoDefault,
                     boolean requiereAprobacion, String descripcion) {
        this.nombre = nombre;
        this.abreviacion = abreviacion;
        this.pesoDefault = pesoDefault;
        this.requiereAprobacion = requiereAprobacion;
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el nombre del tipo de calificación
     * @return String con el nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene la abreviación del tipo
     * @return String con la abreviación
     */
    public String getAbreviacion() {
        return abreviacion;
    }

    /**
     * Obtiene el peso porcentual por defecto
     * @return double con el peso
     */
    public double getPesoDefault() {
        return pesoDefault;
    }

    /**
     * Verifica si requiere aprobación mínima
     * @return true si requiere aprobación
     */
    public boolean isRequiereAprobacion() {
        return requiereAprobacion;
    }

    /**
     * Obtiene la descripción del tipo
     * @return String con la descripción
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Verifica si es un tipo de examen
     * @return true si es examen
     */
    public boolean esExamen() {
        return this == EXAMEN_PARCIAL || this == EXAMEN_FINAL ||
                this == EXTRAORDINARIO || this == TITULO;
    }

    /**
     * Verifica si es una actividad continua
     * @return true si es actividad continua
     */
    public boolean esActividadContinua() {
        return this == TAREA || this == PARTICIPACION || this == PRACTICA ||
                this == QUIZ;
    }

    /**
     * Verifica si es un trabajo mayor
     * @return true si es trabajo mayor
     */
    public boolean esTrabajoMayor() {
        return this == PROYECTO || this == ENSAYO || this == PRESENTACION ||
                this == LABORATORIO;
    }

    /**
     * Obtiene la calificación mínima aprobatoria según el tipo
     * @return double con la calificación mínima
     */
    public double getCalificacionMinima() {
        if (esExamen()) {
            return 6.0; // Mínimo para exámenes
        } else if (esTrabajoMayor()) {
            return 7.0; // Mínimo para trabajos mayores
        } else {
            return 6.0; // Mínimo general
        }
    }

    /**
     * Obtiene los tipos de calificación para evaluación continua
     * @return Array de tipos para evaluación continua
     */
    public static TipoCalificacion[] getTiposEvaluacionContinua() {
        return new TipoCalificacion[]{
                TAREA, PARTICIPACION, PRACTICA, QUIZ
        };
    }

    /**
     * Obtiene los tipos de calificación para evaluaciones importantes
     * @return Array de tipos para evaluaciones importantes
     */
    public static TipoCalificacion[] getTiposEvaluacionImportante() {
        return new TipoCalificacion[]{
                EXAMEN_PARCIAL, EXAMEN_FINAL, PROYECTO, ENSAYO,
                PRESENTACION, LABORATORIO
        };
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
     * @return TipoCalificacion encontrado o null si no existe
     */
    public static TipoCalificacion fromNombre(String nombre) {
        for (TipoCalificacion tipo : values()) {
            if (tipo.getNombre().equalsIgnoreCase(nombre)) {
                return tipo;
            }
        }
        return null;
    }

    /**
     * Obtiene un tipo por su abreviación
     * @param abreviacion Abreviación del tipo a buscar
     * @return TipoCalificacion encontrado o null si no existe
     */
    public static TipoCalificacion fromAbreviacion(String abreviacion) {
        for (TipoCalificacion tipo : values()) {
            if (tipo.getAbreviacion().equalsIgnoreCase(abreviacion)) {
                return tipo;
            }
        }
        return null;
    }
}