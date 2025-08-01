// Profesor.java
package com.controlescolar.models;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.util.List;

public class Profesor {
    private ObjectId id;
    private String numeroEmpleado;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private String especialidad;
    private List<ObjectId> materiasIds;
    private boolean activo;
    private LocalDateTime fechaIngreso;
    private String foto;
    
    // Additional fields for UI compatibility
    private String cedula;
    private String direccion;
    private String especialidades;
    private String estatus;

    // Constructores
    public Profesor() {
        this.id = new ObjectId();
        this.fechaIngreso = LocalDateTime.now();
        this.activo = true;
    }

    public Profesor(String numeroEmpleado, String nombre, String apellidos, String email) {
        this();
        this.numeroEmpleado = numeroEmpleado;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
    }

    // Métodos para conversión Document
    public Document toDocument() {
        return new Document("_id", id)
                .append("numeroEmpleado", numeroEmpleado)
                .append("nombre", nombre)
                .append("apellidos", apellidos)
                .append("email", email)
                .append("telefono", telefono)
                .append("especialidad", especialidad)
                .append("materiasIds", materiasIds)
                .append("activo", activo)
                .append("fechaIngreso", fechaIngreso != null ? 
                    java.util.Date.from(fechaIngreso.atZone(java.time.ZoneId.systemDefault()).toInstant()) : null)
                .append("foto", foto);
    }

    public static Profesor fromDocument(Document doc) {
        Profesor profesor = new Profesor();
        profesor.setId(doc.getObjectId("_id"));
        profesor.setNumeroEmpleado(doc.getString("numeroEmpleado"));
        profesor.setNombre(doc.getString("nombre"));
        profesor.setApellidos(doc.getString("apellidos"));
        profesor.setEmail(doc.getString("email"));
        profesor.setTelefono(doc.getString("telefono"));
        profesor.setEspecialidad(doc.getString("especialidad"));
        profesor.setMateriasIds(doc.getList("materiasIds", ObjectId.class));
        profesor.setActivo(doc.getBoolean("activo", true));
        
        // Convertir fechaIngreso de Date a LocalDateTime
        java.util.Date fechaIngresoDate = doc.getDate("fechaIngreso");
        if (fechaIngresoDate != null) {
            profesor.setFechaIngreso(fechaIngresoDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        
        profesor.setFoto(doc.getString("foto"));
        return profesor;
    }

    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getNumeroEmpleado() { return numeroEmpleado; }
    public void setNumeroEmpleado(String numeroEmpleado) { this.numeroEmpleado = numeroEmpleado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    
    public String getApellido() { return apellidos; } // Alias for UI compatibility

    public String getNombreCompleto() { return nombre + " " + apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public List<ObjectId> getMateriasIds() { return materiasIds; }
    public void setMateriasIds(List<ObjectId> materiasIds) { this.materiasIds = materiasIds; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    // Additional getters and setters for UI compatibility
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEspecialidades() { return especialidades; }
    public void setEspecialidades(String especialidades) { this.especialidades = especialidades; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public void setApellido(String apellido) { this.apellidos = apellido; } // Alias for UI compatibility
}