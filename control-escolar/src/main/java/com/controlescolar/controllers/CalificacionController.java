// CalificacionController.java
package com.controlescolar.controllers;

import com.controlescolar.models.Calificacion;
import com.controlescolar.enums.TipoCalificacion;
import com.controlescolar.utils.DatabaseUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

public class CalificacionController {
    private static MongoCollection<Document> collection = DatabaseUtil.getCollection("calificaciones");

    public static boolean registrarCalificacion(Calificacion calificacion) {
        try {
            collection.insertOne(calificacion.toDocument());
            return true;
        } catch (Exception e) {
            System.err.println("Error al registrar calificación: " + e.getMessage());
            return false;
        }
    }

    public static boolean actualizarCalificacion(Calificacion calificacion) {
        try {
            calificacion.setFechaModificacion(java.time.LocalDateTime.now());
            Document updateDoc = new Document()
                    .append("calificacion", calificacion.getCalificacion())
                    .append("observaciones", calificacion.getObservaciones())
                    .append("fechaModificacion", calificacion.getFechaModificacion());

            collection.updateOne(
                    Filters.eq("_id", calificacion.getId()),
                    new Document("$set", updateDoc)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar calificación: " + e.getMessage());
            return false;
        }
    }

    public static List<Calificacion> obtenerCalificacionesPorAlumno(ObjectId alumnoId) {
        List<Calificacion> calificaciones = new ArrayList<>();
        try {
            collection.find(Filters.eq("alumnoId", alumnoId))
                    .sort(Sorts.descending("fechaRegistro"))
                    .forEach(doc -> calificaciones.add(Calificacion.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener calificaciones por alumno: " + e.getMessage());
        }
        return calificaciones;
    }

    public static List<Calificacion> obtenerCalificacionesPorMateria(ObjectId materiaId) {
        List<Calificacion> calificaciones = new ArrayList<>();
        try {
            collection.find(Filters.eq("materiaId", materiaId))
                    .sort(Sorts.descending("fechaRegistro"))
                    .forEach(doc -> calificaciones.add(Calificacion.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener calificaciones por materia: " + e.getMessage());
        }
        return calificaciones;
    }

    public static List<Calificacion> obtenerCalificacionesPorAlumnoYMateria(ObjectId alumnoId, ObjectId materiaId) {
        List<Calificacion> calificaciones = new ArrayList<>();
        try {
            Document filter = new Document("alumnoId", alumnoId).append("materiaId", materiaId);
            collection.find(filter)
                    .sort(Sorts.ascending("tipo"))
                    .forEach(doc -> calificaciones.add(Calificacion.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener calificaciones por alumno y materia: " + e.getMessage());
        }
        return calificaciones;
    }

    public static double calcularPromedioAlumno(ObjectId alumnoId, ObjectId materiaId) {
        try {
            List<Calificacion> calificaciones = obtenerCalificacionesPorAlumnoYMateria(alumnoId, materiaId);
            if (calificaciones.isEmpty()) return 0.0;

            double sumaFinal = 0.0;
            int countParciales = 0, countExamenes = 0, countTareas = 0;
            double sumaParciales = 0.0, sumaExamenes = 0.0, sumaTareas = 0.0;

            for (Calificacion cal : calificaciones) {
                switch (cal.getTipo()) {
                    case PARCIAL:
                        sumaParciales += cal.getCalificacion();
                        countParciales++;
                        break;
                    case EXAMEN_FINAL:
                        sumaExamenes += cal.getCalificacion();
                        countExamenes++;
                        break;
                    case TAREA:
                        sumaTareas += cal.getCalificacion();
                        countTareas++;
                        break;
                }
            }

            // Pesos: Parciales 40%, Examen Final 40%, Tareas 20%
            if (countParciales > 0) sumaFinal += (sumaParciales / countParciales) * 0.4;
            if (countExamenes > 0) sumaFinal += (sumaExamenes / countExamenes) * 0.4;
            if (countTareas > 0) sumaFinal += (sumaTareas / countTareas) * 0.2;

            return Math.round(sumaFinal * 100.0) / 100.0;
        } catch (Exception e) {
            System.err.println("Error al calcular promedio: " + e.getMessage());
            return 0.0;
        }
    }

    public static boolean eliminarCalificacion(ObjectId id) {
        try {
            collection.deleteOne(Filters.eq("_id", id));
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar calificación: " + e.getMessage());
            return false;
        }
    }
}