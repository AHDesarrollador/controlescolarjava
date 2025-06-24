// AsistenciaController.java
package com.controlescolar.controllers;

import com.controlescolar.models.Asistencia;
import com.controlescolar.utils.DatabaseUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AsistenciaController {
    private static MongoCollection<Document> collection = DatabaseUtil.getCollection("asistencias");

    public static boolean registrarAsistencia(Asistencia asistencia) {
        try {
            // Verificar si ya existe asistencia para esa fecha, alumno y materia
            Document filter = new Document("alumnoId", asistencia.getAlumnoId())
                    .append("materiaId", asistencia.getMateriaId())
                    .append("fecha", asistencia.getFecha());

            if (collection.find(filter).first() != null) {
                return actualizarAsistencia(asistencia);
            }

            collection.insertOne(asistencia.toDocument());
            return true;
        } catch (Exception e) {
            System.err.println("Error al registrar asistencia: " + e.getMessage());
            return false;
        }
    }

    public static boolean actualizarAsistencia(Asistencia asistencia) {
        try {
            Document filter = new Document("alumnoId", asistencia.getAlumnoId())
                    .append("materiaId", asistencia.getMateriaId())
                    .append("fecha", asistencia.getFecha());

            Document updateDoc = new Document()
                    .append("estado", asistencia.getEstado().toString())
                    .append("observaciones", asistencia.getObservaciones());

            collection.updateOne(filter, new Document("$set", updateDoc));
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar asistencia: " + e.getMessage());
            return false;
        }
    }

    public static List<Asistencia> obtenerAsistenciasPorAlumno(ObjectId alumnoId) {
        List<Asistencia> asistencias = new ArrayList<>();
        try {
            collection.find(Filters.eq("alumnoId", alumnoId))
                    .sort(Sorts.descending("fecha"))
                    .forEach(doc -> asistencias.add(Asistencia.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener asistencias por alumno: " + e.getMessage());
        }
        return asistencias;
    }

    public static List<Asistencia> obtenerAsistenciasPorMateria(ObjectId materiaId, LocalDate fecha) {
        List<Asistencia> asistencias = new ArrayList<>();
        try {
            Document filter = new Document("materiaId", materiaId).append("fecha", fecha);
            collection.find(filter)
                    .forEach(doc -> asistencias.add(Asistencia.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener asistencias por materia: " + e.getMessage());
        }
        return asistencias;
    }

    public static List<Asistencia> obtenerAsistenciasPorPeriodo(ObjectId alumnoId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Asistencia> asistencias = new ArrayList<>();
        try {
            Document filter = new Document("alumnoId", alumnoId)
                    .append("fecha", new Document("$gte", fechaInicio).append("$lte", fechaFin));

            collection.find(filter)
                    .sort(Sorts.ascending("fecha"))
                    .forEach(doc -> asistencias.add(Asistencia.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener asistencias por periodo: " + e.getMessage());
        }
        return asistencias;
    }

    public static double calcularPorcentajeAsistencia(ObjectId alumnoId, ObjectId materiaId, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            Document filter = new Document("alumnoId", alumnoId)
                    .append("materiaId", materiaId)
                    .append("fecha", new Document("$gte", fechaInicio).append("$lte", fechaFin));

            List<Asistencia> asistencias = new ArrayList<>();
            collection.find(filter).forEach(doc -> asistencias.add(Asistencia.fromDocument(doc)));

            if (asistencias.isEmpty()) return 0.0;

            long presentes = asistencias.stream().mapToLong(a -> a.getEstado() == com.controlescolar.enums.EstadoAsistencia.PRESENTE ? 1 : 0).sum();
            return (double) presentes / asistencias.size() * 100.0;

        } catch (Exception e) {
            System.err.println("Error al calcular porcentaje de asistencia: " + e.getMessage());
            return 0.0;
        }
    }
}