// Materia.java
package com.controlescolar.models;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;

public class Materia {
    private ObjectId id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private int creditos;
    private ObjectId profesorId;
    private boolean activa;
    private LocalDateTime fechaCreacion;
    
    // Additional fields for UI compatibility
    private int horasSemanales;
    private String semestre;
    private String prerequisitos;
    private Profesor profesor;
    private String estatus;

    // Constructores
    public Materia() {
        this.id = new ObjectId();
        this.fechaCreacion = LocalDateTime.now();
        this.activa = true;
    }

    public Materia(String codigo, String nombre, String descripcion, int creditos) {
        this();
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.creditos = creditos;
    }

    // Métodos para conversión Document
    public Document toDocument() {
        return new Document("_id", id)
                .append("codigo", codigo)
                .append("nombre", nombre)
                .append("descripcion", descripcion)
                .append("creditos", creditos)
                .append("profesorId", profesorId)
                .append("activa", activa)
                .append("fechaCreacion", fechaCreacion != null ? 
                    java.util.Date.from(fechaCreacion.atZone(java.time.ZoneId.systemDefault()).toInstant()) : null);
    }

    public static Materia fromDocument(Document doc) {
        Materia materia = new Materia();
        materia.setId(doc.getObjectId("_id"));
        materia.setCodigo(doc.getString("codigo"));
        materia.setNombre(doc.getString("nombre"));
        materia.setDescripcion(doc.getString("descripcion"));
        materia.setCreditos(doc.getInteger("creditos", 0));
        materia.setProfesorId(doc.getObjectId("profesorId"));
        materia.setActiva(doc.getBoolean("activa", true));
        
        // Convertir fechaCreacion de Date a LocalDateTime
        java.util.Date fechaCreacionDate = doc.getDate("fechaCreacion");
        if (fechaCreacionDate != null) {
            materia.setFechaCreacion(fechaCreacionDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        
        return materia;
    }

    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getCreditos() { return creditos; }
    public void setCreditos(int creditos) { this.creditos = creditos; }

    public ObjectId getProfesorId() { return profesorId; }
    public void setProfesorId(ObjectId profesorId) { this.profesorId = profesorId; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    // Additional getters and setters for UI compatibility
    public int getHorasSemanales() { return horasSemanales; }
    public void setHorasSemanales(int horasSemanales) { this.horasSemanales = horasSemanales; }

    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }

    public String getPrerequisitos() { return prerequisitos; }
    public void setPrerequisitos(String prerequisitos) { this.prerequisitos = prerequisitos; }

    public Profesor getProfesor() { return profesor; }
    public void setProfesor(Profesor profesor) { this.profesor = profesor; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
}
