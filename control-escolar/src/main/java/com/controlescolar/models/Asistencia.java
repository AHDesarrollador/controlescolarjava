// Asistencia.java
package com.controlescolar.models;

import com.controlescolar.enums.EstadoAsistencia;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Asistencia {
    private ObjectId id;
    private ObjectId alumnoId;
    private ObjectId grupoId;
    private ObjectId materiaId;
    private LocalDate fecha;
    private EstadoAsistencia estado;
    private String observaciones;
    private LocalDateTime fechaRegistro;

    // Constructores
    public Asistencia() {
        this.id = new ObjectId();
        this.fechaRegistro = LocalDateTime.now();
        this.fecha = LocalDate.now();
    }

    public Asistencia(ObjectId alumnoId, ObjectId grupoId, ObjectId materiaId, EstadoAsistencia estado) {
        this();
        this.alumnoId = alumnoId;
        this.grupoId = grupoId;
        this.materiaId = materiaId;
        this.estado = estado;
    }

    // Métodos para conversión Document
    public Document toDocument() {
        return new Document("_id", id)
                .append("alumnoId", alumnoId)
                .append("grupoId", grupoId)
                .append("materiaId", materiaId)
                .append("fecha", fecha != null ? 
                    Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null)
                .append("estado", estado.name())
                .append("observaciones", observaciones)
                .append("fechaRegistro", fechaRegistro != null ? 
                    Date.from(fechaRegistro.atZone(ZoneId.systemDefault()).toInstant()) : null);
    }

    public static Asistencia fromDocument(Document doc) {
        Asistencia asistencia = new Asistencia();
        asistencia.setId(doc.getObjectId("_id"));
        asistencia.setAlumnoId(doc.getObjectId("alumnoId"));
        asistencia.setGrupoId(doc.getObjectId("grupoId"));
        asistencia.setMateriaId(doc.getObjectId("materiaId"));
        // Convertir fecha de Date a LocalDate
        java.util.Date fechaDate = doc.getDate("fecha");
        if (fechaDate != null) {
            asistencia.setFecha(fechaDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate());
        }
        
        // Parse estado with fallback for descripcion-based values
        String estadoStr = doc.getString("estado");
        EstadoAsistencia estado = null;
        try {
            estado = EstadoAsistencia.valueOf(estadoStr);
        } catch (IllegalArgumentException e) {
            // Try by descripcion if valueOf fails
            estado = EstadoAsistencia.fromDescripcion(estadoStr);
            if (estado == null) {
                // Default fallback
                estado = EstadoAsistencia.AUSENTE;
            }
        }
        asistencia.setEstado(estado);
        asistencia.setObservaciones(doc.getString("observaciones"));
        
        // Convertir fechaRegistro de Date a LocalDateTime
        java.util.Date fechaRegistroDate = doc.getDate("fechaRegistro");
        if (fechaRegistroDate != null) {
            asistencia.setFechaRegistro(fechaRegistroDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        return asistencia;
    }

    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getAlumnoId() { return alumnoId; }
    public void setAlumnoId(ObjectId alumnoId) { this.alumnoId = alumnoId; }

    public ObjectId getGrupoId() { return grupoId; }
    public void setGrupoId(ObjectId grupoId) { this.grupoId = grupoId; }

    public ObjectId getMateriaId() { return materiaId; }
    public void setMateriaId(ObjectId materiaId) { this.materiaId = materiaId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public EstadoAsistencia getEstado() { return estado; }
    public void setEstado(EstadoAsistencia estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    // Additional fields and methods for UI compatibility
    private String nombreAlumno;
    private String matriculaAlumno;
    private String nombreMateria;
    private String nombreGrupo;
    private boolean presente;
    private Alumno alumno;
    private Materia materia;

    public String getNombreAlumno() { return nombreAlumno; }
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }

    public String getMatriculaAlumno() { return matriculaAlumno; }
    public void setMatriculaAlumno(String matriculaAlumno) { this.matriculaAlumno = matriculaAlumno; }

    public String getNombreMateria() { return nombreMateria; }
    public void setNombreMateria(String nombreMateria) { this.nombreMateria = nombreMateria; }

    public String getNombreGrupo() { return nombreGrupo; }
    public void setNombreGrupo(String nombreGrupo) { this.nombreGrupo = nombreGrupo; }

    public boolean isPresente() { return presente; }
    public void setPresente(boolean presente) { this.presente = presente; }

    // Method to set estado from string (for UI compatibility)
    public void setEstado(String estadoStr) {
        try {
            this.estado = EstadoAsistencia.valueOf(estadoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle string states from UI
            if ("Presente".equals(estadoStr)) {
                this.estado = EstadoAsistencia.PRESENTE;
                this.presente = true;
            } else if ("Ausente".equals(estadoStr)) {
                this.estado = EstadoAsistencia.AUSENTE;
                this.presente = false;
            }
        }
    }

    // Override getEstado to return string for UI compatibility
    public String getEstadoString() {
        if (estado == null) return "Ausente";
        return estado == EstadoAsistencia.PRESENTE ? "Presente" : "Ausente";
    }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public Materia getMateria() { return materia; }
    public void setMateria(Materia materia) { this.materia = materia; }
}
