// Grupo.java
package com.controlescolar.models;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class Grupo {
    private ObjectId id;
    private String codigo;
    private String nombre;
    private String grado;
    private String seccion;
    private List<ObjectId> alumnosIds;
    private List<ObjectId> materiasIds;
    private ObjectId profesorTitularId;
    private boolean activo;
    private LocalDateTime fechaCreacion;

    // Constructores
    public Grupo() {
        this.id = new ObjectId();
        this.fechaCreacion = LocalDateTime.now();
        this.activo = true;
    }

    public Grupo(String codigo, String nombre, String grado, String seccion) {
        this();
        this.codigo = codigo;
        this.nombre = nombre;
        this.grado = grado;
        this.seccion = seccion;
    }

    // Métodos para conversión Document
    public Document toDocument() {
        return new Document("_id", id)
                .append("codigo", codigo)
                .append("nombre", nombre)
                .append("grado", grado)
                .append("seccion", seccion)
                .append("alumnosIds", alumnosIds)
                .append("materiasIds", materiasIds)
                .append("profesorTitularId", profesorTitularId)
                .append("activo", activo)
                .append("fechaCreacion", fechaCreacion != null ? 
                    Date.from(fechaCreacion.atZone(ZoneId.systemDefault()).toInstant()) : null);
    }

    public static Grupo fromDocument(Document doc) {
        Grupo grupo = new Grupo();
        grupo.setId(doc.getObjectId("_id"));
        grupo.setCodigo(doc.getString("codigo"));
        grupo.setNombre(doc.getString("nombre"));
        grupo.setGrado(doc.getString("grado"));
        grupo.setSeccion(doc.getString("seccion"));
        grupo.setAlumnosIds(doc.getList("alumnosIds", ObjectId.class));
        grupo.setMateriasIds(doc.getList("materiasIds", ObjectId.class));
        grupo.setProfesorTitularId(doc.getObjectId("profesorTitularId"));
        grupo.setActivo(doc.getBoolean("activo", true));
        // Convertir fechaCreacion de Date a LocalDateTime
        java.util.Date fechaCreacionDate = doc.getDate("fechaCreacion");
        if (fechaCreacionDate != null) {
            grupo.setFechaCreacion(fechaCreacionDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        return grupo;
    }

    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getGrado() { return grado; }
    public void setGrado(String grado) { this.grado = grado; }

    public String getSeccion() { return seccion; }
    public void setSeccion(String seccion) { this.seccion = seccion; }

    public List<ObjectId> getAlumnosIds() { return alumnosIds; }
    public void setAlumnosIds(List<ObjectId> alumnosIds) { this.alumnosIds = alumnosIds; }

    public List<ObjectId> getMateriasIds() { return materiasIds; }
    public void setMateriasIds(List<ObjectId> materiasIds) { this.materiasIds = materiasIds; }

    public ObjectId getProfesorTitularId() { return profesorTitularId; }
    public void setProfesorTitularId(ObjectId profesorTitularId) { this.profesorTitularId = profesorTitularId; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
