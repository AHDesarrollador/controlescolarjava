// ProfesorController.java
package com.controlescolar.controllers;

import com.controlescolar.models.Profesor;
import com.controlescolar.utils.DatabaseUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

public class ProfesorController {
    private static MongoCollection<Document> collection = DatabaseUtil.getCollection("profesores");

    public static boolean crearProfesor(Profesor profesor) {
        try {
            // Verificar si el número de empleado ya existe
            if (collection.find(Filters.eq("numeroEmpleado", profesor.getNumeroEmpleado())).first() != null) {
                return false; // Número de empleado ya existe
            }

            collection.insertOne(profesor.toDocument());
            return true;
        } catch (Exception e) {
            System.err.println("Error al crear profesor: " + e.getMessage());
            return false;
        }
    }

    public static boolean actualizarProfesor(Profesor profesor) {
        try {
            Document updateDoc = new Document()
                    .append("nombre", profesor.getNombre())
                    .append("apellidos", profesor.getApellidos())
                    .append("email", profesor.getEmail())
                    .append("telefono", profesor.getTelefono())
                    .append("especialidad", profesor.getEspecialidad())
                    .append("titulo", profesor.getTitulo())
                    .append("experiencia", profesor.getExperiencia())
                    .append("salario", profesor.getSalario())
                    .append("activo", profesor.isActivo());

            collection.updateOne(
                    Filters.eq("_id", profesor.getId()),
                    new Document("$set", updateDoc)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar profesor: " + e.getMessage());
            return false;
        }
    }

    public static List<Profesor> obtenerProfesores() {
        List<Profesor> profesores = new ArrayList<>();
        try {
            collection.find(Filters.eq("activo", true))
                    .forEach(doc -> profesores.add(Profesor.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener profesores: " + e.getMessage());
        }
        return profesores;
    }

    public static Profesor obtenerProfesorPorId(ObjectId id) {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
            return doc != null ? Profesor.fromDocument(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener profesor por ID: " + e.getMessage());
            return null;
        }
    }

    public static Profesor obtenerProfesorPorNumeroEmpleado(String numeroEmpleado) {
        try {
            Document doc = collection.find(Filters.eq("numeroEmpleado", numeroEmpleado)).first();
            return doc != null ? Profesor.fromDocument(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener profesor por número de empleado: " + e.getMessage());
            return null;
        }
    }

    public static List<Profesor> buscarProfesores(String termino) {
        List<Profesor> profesores = new ArrayList<>();
        try {
            Document regex = new Document("$regex", termino).append("$options", "i");
            Document filter = new Document("$or", List.of(
                    new Document("nombre", regex),
                    new Document("apellidos", regex),
                    new Document("numeroEmpleado", regex),
                    new Document("especialidad", regex)
            )).append("activo", true);

            collection.find(filter).forEach(doc -> profesores.add(Profesor.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al buscar profesores: " + e.getMessage());
        }
        return profesores;
    }

    public static boolean eliminarProfesor(ObjectId id) {
        try {
            collection.updateOne(
                    Filters.eq("_id", id),
                    Updates.set("activo", false)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar profesor: " + e.getMessage());
            return false;
        }
    }
}