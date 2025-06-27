// Calificacion.java
package com.controlescolar.models;

import com.controlescolar.enums.TipoCalificacion;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Calificacion {
    private ObjectId id;
    private ObjectId alumnoId;
    private ObjectId materiaId;
    private ObjectId profesorId;
    private TipoCalificacion tipo;
    private double calificacion;
    private double ponderacion;
    private String descripcion;
    private String periodo;
    private String observaciones;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaModificacion;
    
    // Additional fields for UI compatibility
    private Alumno alumno;
    private Materia materia;
    private Grupo grupo;
    private LocalDateTime fecha;

    // Constructores
    public Calificacion() {
        this.id = new ObjectId();
        this.fechaRegistro = LocalDateTime.now();
    }

    public Calificacion(ObjectId alumnoId, ObjectId materiaId, ObjectId profesorId,
                        TipoCalificacion tipo, double calificacion, String periodo) {
        this();
        this.alumnoId = alumnoId;
        this.materiaId = materiaId;
        this.profesorId = profesorId;
        this.tipo = tipo;
        this.calificacion = calificacion;
        this.periodo = periodo;
    }

    // Métodos para conversión Document
    public Document toDocument() {
        return new Document("_id", id)
                .append("alumnoId", alumnoId)
                .append("materiaId", materiaId)
                .append("profesorId", profesorId)
                .append("tipo", tipo.name())
                .append("calificacion", calificacion)
                .append("ponderacion", ponderacion)
                .append("descripcion", descripcion)
                .append("periodo", periodo)
                .append("observaciones", observaciones)
                .append("fechaRegistro", fechaRegistro != null ? 
                    Date.from(fechaRegistro.atZone(ZoneId.systemDefault()).toInstant()) : null)
                .append("fechaModificacion", fechaModificacion != null ? 
                    Date.from(fechaModificacion.atZone(ZoneId.systemDefault()).toInstant()) : null);
    }

    public static Calificacion fromDocument(Document doc) {
        Calificacion calificacion = new Calificacion();
        calificacion.setId(doc.getObjectId("_id"));
        calificacion.setAlumnoId(doc.getObjectId("alumnoId"));
        calificacion.setMateriaId(doc.getObjectId("materiaId"));
        calificacion.setProfesorId(doc.getObjectId("profesorId"));
        // Parse tipo with fallback for nombre-based values
        String tipoStr = doc.getString("tipo");
        TipoCalificacion tipo = null;
        try {
            tipo = TipoCalificacion.valueOf(tipoStr);
        } catch (IllegalArgumentException e) {
            // Try by nombre if valueOf fails
            tipo = TipoCalificacion.fromNombre(tipoStr);
            if (tipo == null) {
                // Try by abreviacion as last resort
                tipo = TipoCalificacion.fromAbreviacion(tipoStr);
            }
        }
        calificacion.setTipo(tipo);
        calificacion.setCalificacion(doc.getDouble("calificacion"));
        calificacion.setPonderacion(doc.getDouble("ponderacion"));
        calificacion.setDescripcion(doc.getString("descripcion"));
        calificacion.setPeriodo(doc.getString("periodo"));
        calificacion.setObservaciones(doc.getString("observaciones"));
        // Convertir fechaRegistro de Date a LocalDateTime
        java.util.Date fechaRegistroDate = doc.getDate("fechaRegistro");
        if (fechaRegistroDate != null) {
            calificacion.setFechaRegistro(fechaRegistroDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        
        // Convertir fechaModificacion de Date a LocalDateTime
        java.util.Date fechaModificacionDate = doc.getDate("fechaModificacion");
        if (fechaModificacionDate != null) {
            calificacion.setFechaModificacion(fechaModificacionDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        return calificacion;
    }

    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getAlumnoId() { return alumnoId; }
    public void setAlumnoId(ObjectId alumnoId) { this.alumnoId = alumnoId; }

    public ObjectId getMateriaId() { return materiaId; }
    public void setMateriaId(ObjectId materiaId) { this.materiaId = materiaId; }

    public ObjectId getProfesorId() { return profesorId; }
    public void setProfesorId(ObjectId profesorId) { this.profesorId = profesorId; }

    public TipoCalificacion getTipo() { return tipo; }
    public void setTipo(TipoCalificacion tipo) { this.tipo = tipo; }

    public double getCalificacion() { return calificacion; }
    public void setCalificacion(double calificacion) { this.calificacion = calificacion; }

    public double getPonderacion() { return ponderacion; }
    public void setPonderacion(double ponderacion) { this.ponderacion = ponderacion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    // Additional getters and setters for UI compatibility
    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public Materia getMateria() { return materia; }
    public void setMateria(Materia materia) { this.materia = materia; }

    public Grupo getGrupo() { return grupo; }
    public void setGrupo(Grupo grupo) { this.grupo = grupo; }

    public LocalDateTime getFecha() { return fecha != null ? fecha : fechaRegistro; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}