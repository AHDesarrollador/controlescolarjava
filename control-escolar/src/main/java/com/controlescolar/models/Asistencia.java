// Asistencia.java
package com.controlescolar.models;

import com.controlescolar.enums.EstadoAsistencia;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
                .append("fecha", fecha)
                .append("estado", estado.toString())
                .append("observaciones", observaciones)
                .append("fechaRegistro", fechaRegistro);
    }

    public static Asistencia fromDocument(Document doc) {
        Asistencia asistencia = new Asistencia();
        asistencia.setId(doc.getObjectId("_id"));
        asistencia.setAlumnoId(doc.getObjectId("alumnoId"));
        asistencia.setGrupoId(doc.getObjectId("grupoId"));
        asistencia.setMateriaId(doc.getObjectId("materiaId"));
        asistencia.setFecha(doc.get("fecha", LocalDate.class));
        asistencia.setEstado(EstadoAsistencia.valueOf(doc.getString("estado")));
        asistencia.setObservaciones(doc.getString("observaciones"));
        asistencia.setFechaRegistro(doc.get("fechaRegistro", LocalDateTime.class));
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
}
