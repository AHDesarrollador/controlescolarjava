// Alumno.java
package com.controlescolar.models;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Alumno {
    private ObjectId id;
    private String matricula;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String nombreTutor;
    private String telefonoTutor;
    private String emailTutor;
    private List<ObjectId> gruposIds;
    private boolean activo;
    private LocalDateTime fechaIngreso;
    private String foto;

    // Constructores
    public Alumno() {
        this.id = new ObjectId();
        this.fechaIngreso = LocalDateTime.now();
        this.activo = true;
    }

    public Alumno(String matricula, String nombre, String apellidos, String email) {
        this();
        this.matricula = matricula;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
    }

    // Métodos para conversión Document
    public Document toDocument() {
        return new Document("_id", id)
                .append("matricula", matricula)
                .append("nombre", nombre)
                .append("apellidos", apellidos)
                .append("email", email)
                .append("telefono", telefono)
                .append("fechaNacimiento", fechaNacimiento != null ? 
                    java.util.Date.from(fechaNacimiento.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()) : null)
                .append("direccion", direccion)
                .append("nombreTutor", nombreTutor)
                .append("telefonoTutor", telefonoTutor)
                .append("emailTutor", emailTutor)
                .append("gruposIds", gruposIds)
                .append("activo", activo)
                .append("fechaIngreso", fechaIngreso != null ? 
                    java.util.Date.from(fechaIngreso.atZone(java.time.ZoneId.systemDefault()).toInstant()) : null)
                .append("foto", foto);
    }

    public static Alumno fromDocument(Document doc) {
        Alumno alumno = new Alumno();
        alumno.setId(doc.getObjectId("_id"));
        alumno.setMatricula(doc.getString("matricula"));
        alumno.setNombre(doc.getString("nombre"));
        alumno.setApellidos(doc.getString("apellidos"));
        alumno.setEmail(doc.getString("email"));
        alumno.setTelefono(doc.getString("telefono"));
        
        // Convertir fechaNacimiento de Date a LocalDate
        java.util.Date fechaNacimientoDate = doc.getDate("fechaNacimiento");
        if (fechaNacimientoDate != null) {
            alumno.setFechaNacimiento(fechaNacimientoDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate());
        }
        
        alumno.setDireccion(doc.getString("direccion"));
        alumno.setNombreTutor(doc.getString("nombreTutor"));
        alumno.setTelefonoTutor(doc.getString("telefonoTutor"));
        alumno.setEmailTutor(doc.getString("emailTutor"));
        alumno.setGruposIds(doc.getList("gruposIds", ObjectId.class));
        alumno.setActivo(doc.getBoolean("activo", true));
        
        // Convertir fechaIngreso de Date a LocalDateTime
        java.util.Date fechaIngresoDate = doc.getDate("fechaIngreso");
        if (fechaIngresoDate != null) {
            alumno.setFechaIngreso(fechaIngresoDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        
        alumno.setFoto(doc.getString("foto"));
        return alumno;
    }

    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getNombreCompleto() { return nombre + " " + apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getNombreTutor() { return nombreTutor; }
    public void setNombreTutor(String nombreTutor) { this.nombreTutor = nombreTutor; }

    public String getTelefonoTutor() { return telefonoTutor; }
    public void setTelefonoTutor(String telefonoTutor) { this.telefonoTutor = telefonoTutor; }

    public String getEmailTutor() { return emailTutor; }
    public void setEmailTutor(String emailTutor) { this.emailTutor = emailTutor; }

    public List<ObjectId> getGruposIds() { return gruposIds; }
    public void setGruposIds(List<ObjectId> gruposIds) { this.gruposIds = gruposIds; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    // Additional methods for UI compatibility
    public String getNumeroControl() { return matricula; } // Alias for matricula
}