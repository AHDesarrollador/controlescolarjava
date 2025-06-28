package com.controlescolar.enums;

/**
 * Enum que define los diferentes roles de usuario en el sistema de control escolar
 */
public enum Rol {
    ADMINISTRADOR("Administrador", "Acceso completo al sistema"),
    DIRECTOR("Director", "Gestión académica y administrativa"),
    PROFESOR("Profesor", "Gestión de materias y calificaciones"),
    ALUMNO("Alumno", "Consulta de información académica"),
    PADRE_FAMILIA("Padre de Familia", "Consulta de información del alumno"),
    SECRETARIO("Secretario", "Gestión administrativa y pagos");

    private final String nombre;
    private final String descripcion;

    /**
     * Constructor del enum Rol
     * @param nombre Nombre del rol
     * @param descripcion Descripción de los permisos del rol
     */
    Rol(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el nombre del rol
     * @return String con el nombre del rol
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene la descripción del rol
     * @return String con la descripción del rol
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Verifica si el rol tiene permisos administrativos
     * @return true si el rol es administrativo
     */
    public boolean esAdministrativo() {
        return this == ADMINISTRADOR || this == DIRECTOR || this == SECRETARIO;
    }

    /**
     * Verifica si el rol puede gestionar calificaciones
     * @return true si puede gestionar calificaciones
     */
    public boolean puedeGestionarCalificaciones() {
        return this == ADMINISTRADOR || this == DIRECTOR || this == PROFESOR;
    }

    /**
     * Verifica si el rol puede ver reportes
     * @return true si puede ver reportes
     */
    public boolean puedeVerReportes() {
        return this != ALUMNO && this != PADRE_FAMILIA;
    }

    /**
     * Convierte el enum a string para mostrar en interfaces
     * @return String con el nombre del rol
     */
    @Override
    public String toString() {
        return nombre;
    }

    /**
     * Obtiene un rol por su nombre
     * @param nombre Nombre del rol a buscar
     * @return Rol encontrado o null si no existe
     */
    public static Rol fromNombre(String nombre) {
        for (Rol rol : values()) {
            if (rol.getNombre().equalsIgnoreCase(nombre)) {
                return rol;
            }
        }
        return null;
    }
}