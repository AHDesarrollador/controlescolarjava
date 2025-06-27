// UsuarioController.java
package com.controlescolar.controllers;

import com.controlescolar.models.Usuario;
import com.controlescolar.enums.Rol;
import com.controlescolar.utils.DatabaseUtil;
import com.controlescolar.utils.SecurityUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

public class UsuarioController {
    private static MongoCollection<Document> collection = DatabaseUtil.getCollection("usuarios");

    public static boolean crearUsuario(Usuario usuario) {
        try {
            // Verificar si el email ya existe
            if (collection.find(Filters.eq("email", usuario.getEmail())).first() != null) {
                return false; // Email ya existe
            }

            // Encriptar contraseña
            usuario.setPassword(SecurityUtil.hashPassword(usuario.getPassword()));

            collection.insertOne(usuario.toDocument());
            return true;
        } catch (Exception e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
            return false;
        }
    }

    public static boolean actualizarUsuario(Usuario usuario) {
        try {
            // Verificar permisos según el rol del usuario a actualizar
            if (usuario.getRol() == Rol.ADMINISTRADOR && !AuthController.canManageAllUsers()) {
                return false; // Solo administradores pueden editar otros administradores
            }
            if (usuario.getRol() == Rol.PADRE_FAMILIA && !AuthController.canManageParentUsers()) {
                return false;
            }
            if (usuario.getRol() == Rol.PROFESOR && !AuthController.canManageTeacherUsers()) {
                return false;
            }
            if (!AuthController.canManageNonAdminUsers() && 
                usuario.getRol() != Rol.PADRE_FAMILIA && 
                usuario.getRol() != Rol.PROFESOR && 
                usuario.getRol() != Rol.ALUMNO) {
                return false;
            }
            Document updateDoc = new Document()
                    .append("nombre", usuario.getNombre())
                    .append("apellidos", usuario.getApellidos())
                    .append("telefono", usuario.getTelefono())
                    .append("foto", usuario.getFoto())
                    .append("activo", usuario.isActivo());

            collection.updateOne(
                    Filters.eq("_id", usuario.getId()),
                    new Document("$set", updateDoc)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    public static boolean cambiarPassword(ObjectId usuarioId, String passwordActual, String passwordNueva) {
        try {
            Document userDoc = collection.find(Filters.eq("_id", usuarioId)).first();

            if (userDoc != null && SecurityUtil.verifyPassword(passwordActual, userDoc.getString("password"))) {
                String hashedPassword = SecurityUtil.hashPassword(passwordNueva);
                collection.updateOne(
                        Filters.eq("_id", usuarioId),
                        Updates.set("password", hashedPassword)
                );
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error al cambiar contraseña: " + e.getMessage());
        }
        return false;
    }

    public static List<Usuario> obtenerUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        try {
            collection.find().forEach(doc -> usuarios.add(Usuario.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    public static List<Usuario> obtenerUsuariosPorRol(Rol rol) {
        List<Usuario> usuarios = new ArrayList<>();
        try {
            collection.find(Filters.eq("rol", rol.name()))
                    .forEach(doc -> usuarios.add(Usuario.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener usuarios por rol: " + e.getMessage());
        }
        return usuarios;
    }

    public static Usuario obtenerUsuarioPorId(ObjectId id) {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
            return doc != null ? Usuario.fromDocument(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener usuario por ID: " + e.getMessage());
            return null;
        }
    }

    public static boolean eliminarUsuario(ObjectId id) {
        try {
            // Verificar permisos - obtener el usuario primero para validar
            Usuario usuario = obtenerUsuarioPorId(id);
            if (usuario != null && usuario.getRol() == Rol.ADMINISTRADOR && !AuthController.canManageAllUsers()) {
                return false; // Solo administradores pueden desactivar otros administradores
            }
            if (!AuthController.canManageNonAdminUsers()) {
                return false;
            }
            collection.updateOne(
                    Filters.eq("_id", id),
                    Updates.set("activo", false)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    public static boolean activarUsuario(ObjectId id) {
        try {
            // Verificar permisos - obtener el usuario primero para validar
            Usuario usuario = obtenerUsuarioPorId(id);
            if (usuario != null && usuario.getRol() == Rol.ADMINISTRADOR && !AuthController.canManageAllUsers()) {
                return false; // Solo administradores pueden activar otros administradores
            }
            if (!AuthController.canManageNonAdminUsers()) {
                return false;
            }
            collection.updateOne(
                    Filters.eq("_id", id),
                    Updates.set("activo", true)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al activar usuario: " + e.getMessage());
            return false;
        }
    }

    public static boolean cambiarRol(ObjectId id, Rol nuevoRol) {
        try {
            // Verificar permisos
            if (nuevoRol == Rol.ADMINISTRADOR && !AuthController.canManageAllUsers()) {
                return false; // Solo administradores pueden crear otros administradores
            }
            if (!AuthController.canManageNonAdminUsers()) {
                return false;
            }
            collection.updateOne(
                    Filters.eq("_id", id),
                    Updates.set("rol", nuevoRol.toString())
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al cambiar rol: " + e.getMessage());
            return false;
        }
    }

    public static boolean resetearPassword(ObjectId id, String nuevaPassword) {
        try {
            // Verificar permisos - obtener el usuario primero para validar
            Usuario usuario = obtenerUsuarioPorId(id);
            if (usuario != null && usuario.getRol() == Rol.ADMINISTRADOR && !AuthController.canManageAllUsers()) {
                return false; // Solo administradores pueden resetear contraseñas de otros administradores
            }
            if (!AuthController.canManageNonAdminUsers()) {
                return false;
            }
            String hashedPassword = SecurityUtil.hashPassword(nuevaPassword);
            collection.updateOne(
                    Filters.eq("_id", id),
                    Updates.set("password", hashedPassword)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al resetear contraseña: " + e.getMessage());
            return false;
        }
    }

    public static Usuario obtenerUsuarioPorEmail(String email) {
        try {
            Document doc = collection.find(Filters.eq("email", email)).first();
            return doc != null ? Usuario.fromDocument(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener usuario por email: " + e.getMessage());
            return null;
        }
    }
}