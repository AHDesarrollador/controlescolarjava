// AlumnoController.java (Continuación y completo)
package com.controlescolar.controllers;

import com.controlescolar.models.Alumno;
import com.controlescolar.utils.DatabaseUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

public class AlumnoController {
    private static MongoCollection<Document> collection = DatabaseUtil.getCollection("alumnos");
    

    public static boolean crearAlumno(Alumno alumno) {
        try {
            // Verificar si la matrícula ya existe
            if (collection.find(Filters.eq("matricula", alumno.getMatricula())).first() != null) {
                return false; // Matrícula ya existe
            }

            collection.insertOne(alumno.toDocument());
            return true;
        } catch (Exception e) {
            System.err.println("Error al crear alumno: " + e.getMessage());
            return false;
        }
    }

    public static boolean actualizarAlumno(Alumno alumno) {
        try {
            // Verificar permisos
            if (!AuthController.canManageStudentUsers()) {
                return false;
            }
            Document updateDoc = new Document()
                    .append("nombre", alumno.getNombre())
                    .append("apellidos", alumno.getApellidos())
                    .append("email", alumno.getEmail())
                    .append("telefono", alumno.getTelefono())
                    .append("direccion", alumno.getDireccion())
                    .append("fechaNacimiento", alumno.getFechaNacimiento())
                    .append("nombreTutor", alumno.getNombreTutor())
                    .append("telefonoTutor", alumno.getTelefonoTutor())
                    .append("activo", alumno.isActivo());

            collection.updateOne(
                    Filters.eq("_id", alumno.getId()),
                    new Document("$set", updateDoc)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar alumno: " + e.getMessage());
            return false;
        }
    }

    public static List<Alumno> obtenerAlumnos() {
        List<Alumno> alumnos = new ArrayList<>();
        try {
            collection.find(Filters.eq("activo", true))
                    .forEach(doc -> alumnos.add(Alumno.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener alumnos: " + e.getMessage());
        }
        return alumnos;
    }

    public static Alumno obtenerAlumnoPorId(ObjectId id) {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
            return doc != null ? Alumno.fromDocument(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener alumno por ID: " + e.getMessage());
            return null;
        }
    }

    public static Alumno obtenerAlumnoPorMatricula(String matricula) {
        try {
            Document doc = collection.find(Filters.eq("matricula", matricula)).first();
            return doc != null ? Alumno.fromDocument(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener alumno por matrícula: " + e.getMessage());
            return null;
        }
    }

    public static List<Alumno> buscarAlumnos(String termino) {
        List<Alumno> alumnos = new ArrayList<>();
        try {
            Document regex = new Document("$regex", termino).append("$options", "i");
            Document filter = new Document("$or", List.of(
                    new Document("nombre", regex),
                    new Document("apellidos", regex),
                    new Document("matricula", regex),
                    new Document("email", regex)
            )).append("activo", true);

            collection.find(filter).forEach(doc -> alumnos.add(Alumno.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al buscar alumnos: " + e.getMessage());
        }
        return alumnos;
    }

    public static boolean eliminarAlumno(ObjectId id) {
        try {
            collection.updateOne(
                    Filters.eq("_id", id),
                    Updates.set("activo", false)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar alumno: " + e.getMessage());
            return false;
        }
    }

    // Additional methods for UI compatibility
    public List<Alumno> obtenerTodos() {
        return obtenerAlumnos();
    }
}
