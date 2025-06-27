// AuthController.java
package com.controlescolar.controllers;

import com.controlescolar.models.Usuario;
import com.controlescolar.enums.Rol;
import com.controlescolar.utils.DatabaseUtil;
import com.controlescolar.utils.SecurityUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class AuthController {
    private static Usuario usuarioActual;
    private static MongoCollection<Document> collection = DatabaseUtil.getCollection("usuarios");

    public static boolean login(String email, String password) {
        try {
            Document userDoc = collection.find(
                Filters.and(
                    Filters.eq("email", email),
                    Filters.eq("activo", true)
                )
            ).first();

            if (userDoc != null && SecurityUtil.verifyPassword(password, userDoc.getString("password"))) {
                usuarioActual = Usuario.fromDocument(userDoc);
                usuarioActual.setUltimoAcceso(LocalDateTime.now());

                // Actualizar último acceso
                collection.updateOne(
                        Filters.eq("_id", usuarioActual.getId()),
                        new Document("$set", new Document("ultimoAcceso", LocalDateTime.now()))
                );

                return true;
            }
        } catch (Exception e) {
            System.err.println("Error en login: " + e.getMessage());
        }
        return false;
    }

    public static boolean registrarUsuario(Usuario usuario, boolean esInvitacion) {
        try {
            // Solo administradores pueden registrar usuarios o mediante invitación
            if (!esInvitacion && (usuarioActual == null || !usuarioActual.getRol().esAdministrativo())) {
                return false;
            }

            // Verificar si el email ya existe
            if (collection.find(Filters.eq("email", usuario.getEmail())).first() != null) {
                return false;
            }

            return UsuarioController.crearUsuario(usuario);
        } catch (Exception e) {
            System.err.println("Error en registro: " + e.getMessage());
            return false;
        }
    }

    public static void logout() {
        usuarioActual = null;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static boolean isLoggedIn() {
        return usuarioActual != null;
    }

    public static boolean hasRole(String role) {
        return usuarioActual != null && usuarioActual.getRol().toString().equals(role);
    }

    public static boolean hasRole(Rol rol) {
        return usuarioActual != null && usuarioActual.getRol() == rol;
    }

    public static boolean hasAnyRole(Rol... roles) {
        if (usuarioActual == null) return false;
        return Arrays.asList(roles).contains(usuarioActual.getRol());
    }

    // Métodos de autorización específicos
    public static boolean canManageUsers() {
        return usuarioActual != null && usuarioActual.getRol().esAdministrativo();
    }

    public static boolean canManageStudentUsers() {
        return usuarioActual != null && 
               (usuarioActual.getRol() == Rol.ADMINISTRADOR || 
                usuarioActual.getRol() == Rol.DIRECTOR ||
                usuarioActual.getRol() == Rol.SECRETARIO);
    }

    public static boolean canManageTeacherUsers() {
        return usuarioActual != null && 
               (usuarioActual.getRol() == Rol.ADMINISTRADOR || 
                usuarioActual.getRol() == Rol.DIRECTOR ||
                usuarioActual.getRol() == Rol.SECRETARIO);
    }

    public static boolean canManageParentUsers() {
        return usuarioActual != null && 
               (usuarioActual.getRol() == Rol.ADMINISTRADOR || 
                usuarioActual.getRol() == Rol.DIRECTOR ||
                usuarioActual.getRol() == Rol.SECRETARIO);
    }

    public static boolean canManageParentStudentRelations() {
        return usuarioActual != null && 
               (usuarioActual.getRol() == Rol.ADMINISTRADOR || 
                usuarioActual.getRol() == Rol.DIRECTOR ||
                usuarioActual.getRol() == Rol.SECRETARIO);
    }

    public static boolean canManageAllUsers() {
        return usuarioActual != null && usuarioActual.getRol() == Rol.ADMINISTRADOR;
    }

    public static boolean canManageNonAdminUsers() {
        return usuarioActual != null && 
               (usuarioActual.getRol() == Rol.ADMINISTRADOR || 
                usuarioActual.getRol() == Rol.DIRECTOR);
    }

    public static boolean canManageSubjects() {
        return usuarioActual != null && 
               (usuarioActual.getRol() == Rol.ADMINISTRADOR || 
                usuarioActual.getRol() == Rol.DIRECTOR);
    }

    public static boolean canManageGroups() {
        return usuarioActual != null && 
               (usuarioActual.getRol() == Rol.ADMINISTRADOR || 
                usuarioActual.getRol() == Rol.DIRECTOR);
    }

    public static boolean canAccessAllReports() {
        return usuarioActual != null && 
               (usuarioActual.getRol() == Rol.ADMINISTRADOR || 
                usuarioActual.getRol() == Rol.DIRECTOR ||
                usuarioActual.getRol() == Rol.SECRETARIO);
    }

    public static boolean canManageGrades() {
        return usuarioActual != null && usuarioActual.getRol().puedeGestionarCalificaciones();
    }

    public static boolean canViewReports() {
        return usuarioActual != null && usuarioActual.getRol().puedeVerReportes();
    }

    public static boolean canManagePayments() {
        return usuarioActual != null && 
               (usuarioActual.getRol() == Rol.ADMINISTRADOR || 
                usuarioActual.getRol() == Rol.DIRECTOR ||
                usuarioActual.getRol() == Rol.SECRETARIO);
    }

    public static boolean canManageAttendance() {
        return usuarioActual != null && 
               (usuarioActual.getRol().esAdministrativo() || 
                usuarioActual.getRol() == Rol.PROFESOR);
    }

    public static boolean canViewStudentData(String studentId) {
        if (usuarioActual == null) return false;
        
        switch (usuarioActual.getRol()) {
            case ADMINISTRADOR:
            case DIRECTOR:
            case SECRETARIO:
                return true;
            case PROFESOR:
                // Verificar si el profesor tiene asignado al estudiante
                return true; // Implementar lógica específica
            case ALUMNO:
                // Solo pueden ver sus propios datos
                return usuarioActual.getId().toString().equals(studentId);
            case PADRE_FAMILIA:
                // Verificar si es padre del estudiante
                try {
                    ObjectId alumnoId = new ObjectId(studentId);
                    return PadreAlumnoController.puedeAccederAlumno(usuarioActual.getId(), alumnoId);
                } catch (Exception e) {
                    return false;
                }
            default:
                return false;
        }
    }

    public static boolean canViewStudentData(ObjectId studentId) {
        if (usuarioActual == null) return false;
        
        switch (usuarioActual.getRol()) {
            case ADMINISTRADOR:
            case DIRECTOR:
            case SECRETARIO:
                return true;
            case PROFESOR:
                // Verificar si el profesor tiene asignado al estudiante
                return true; // Implementar lógica específica
            case ALUMNO:
                // Solo pueden ver sus propios datos
                return usuarioActual.getId().equals(studentId);
            case PADRE_FAMILIA:
                // Verificar si es padre del estudiante
                return PadreAlumnoController.puedeAccederAlumno(usuarioActual.getId(), studentId);
            default:
                return false;
        }
    }

    public static boolean canManageStudentPayments(ObjectId studentId) {
        if (usuarioActual == null) return false;
        
        switch (usuarioActual.getRol()) {
            case ADMINISTRADOR:
            case SECRETARIO:
                return true;
            case PADRE_FAMILIA:
                // Los padres pueden generar pagos para sus hijos
                return PadreAlumnoController.puedeAccederAlumno(usuarioActual.getId(), studentId);
            default:
                return false;
        }
    }
}