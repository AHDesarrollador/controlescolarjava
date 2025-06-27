package com.controlescolar.models;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;

public class PadreAlumno {
    private ObjectId id;
    private ObjectId padreId;
    private ObjectId alumnoId;
    private String parentesco; // padre, madre, tutor, etc.
    private boolean autorizado; // si está autorizado a ver información
    private LocalDateTime fechaVinculacion;
    private boolean activo;

    public PadreAlumno() {
        this.id = new ObjectId();
        this.fechaVinculacion = LocalDateTime.now();
        this.autorizado = true;
        this.activo = true;
    }

    public PadreAlumno(ObjectId padreId, ObjectId alumnoId, String parentesco) {
        this();
        this.padreId = padreId;
        this.alumnoId = alumnoId;
        this.parentesco = parentesco;
    }

    public Document toDocument() {
        return new Document("_id", id)
                .append("padreId", padreId)
                .append("alumnoId", alumnoId)
                .append("parentesco", parentesco)
                .append("autorizado", autorizado)
                .append("fechaVinculacion", fechaVinculacion)
                .append("activo", activo);
    }

    public static PadreAlumno fromDocument(Document doc) {
        PadreAlumno padreAlumno = new PadreAlumno();
        padreAlumno.setId(doc.getObjectId("_id"));
        padreAlumno.setPadreId(doc.getObjectId("padreId"));
        padreAlumno.setAlumnoId(doc.getObjectId("alumnoId"));
        padreAlumno.setParentesco(doc.getString("parentesco"));
        padreAlumno.setAutorizado(doc.getBoolean("autorizado", true));
        padreAlumno.setActivo(doc.getBoolean("activo", true));
        
        Object fechaVinculacionObj = doc.get("fechaVinculacion");
        if (fechaVinculacionObj instanceof LocalDateTime) {
            padreAlumno.setFechaVinculacion((LocalDateTime) fechaVinculacionObj);
        }
        
        return padreAlumno;
    }

    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getPadreId() { return padreId; }
    public void setPadreId(ObjectId padreId) { this.padreId = padreId; }

    public ObjectId getAlumnoId() { return alumnoId; }
    public void setAlumnoId(ObjectId alumnoId) { this.alumnoId = alumnoId; }

    public String getParentesco() { return parentesco; }
    public void setParentesco(String parentesco) { this.parentesco = parentesco; }

    public boolean isAutorizado() { return autorizado; }
    public void setAutorizado(boolean autorizado) { this.autorizado = autorizado; }

    public LocalDateTime getFechaVinculacion() { return fechaVinculacion; }
    public void setFechaVinculacion(LocalDateTime fechaVinculacion) { this.fechaVinculacion = fechaVinculacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}