// Usuario.java
package com.controlescolar.models;

import com.controlescolar.enums.Rol;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;

public class Usuario {
    private ObjectId id;
    private String email;
    private String password;
    private Rol rol;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String foto;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;

    // Constructores
    public Usuario() {
        this.id = new ObjectId();
        this.fechaCreacion = LocalDateTime.now();
        this.activo = true;
    }

    public Usuario(String email, String password, Rol rol, String nombre, String apellidos) {
        this();
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.nombre = nombre;
        this.apellidos = apellidos;
    }

    // Métodos para conversión Document
    public Document toDocument() {
        return new Document("_id", id)
                .append("email", email)
                .append("password", password)
                .append("rol", rol.toString())
                .append("nombre", nombre)
                .append("apellidos", apellidos)
                .append("telefono", telefono)
                .append("foto", foto)
                .append("activo", activo)
                .append("fechaCreacion", fechaCreacion)
                .append("ultimoAcceso", ultimoAcceso);
    }

    public static Usuario fromDocument(Document doc) {
        Usuario usuario = new Usuario();
        usuario.setId(doc.getObjectId("_id"));
        usuario.setEmail(doc.getString("email"));
        usuario.setPassword(doc.getString("password"));
        usuario.setRol(Rol.valueOf(doc.getString("rol")));
        usuario.setNombre(doc.getString("nombre"));
        usuario.setApellidos(doc.getString("apellidos"));
        usuario.setTelefono(doc.getString("telefono"));
        usuario.setFoto(doc.getString("foto"));
        usuario.setActivo(doc.getBoolean("activo", true));
        usuario.setFechaCreacion(doc.get("fechaCreacion", LocalDateTime.class));
        usuario.setUltimoAcceso(doc.get("ultimoAcceso", LocalDateTime.class));
        return usuario;
    }

    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getNombreCompleto() { return nombre + " " + apellidos; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
}
