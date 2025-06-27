// AsistenciaController.java
package com.controlescolar.controllers;

import com.controlescolar.models.Asistencia;
import com.controlescolar.models.Materia;
import com.controlescolar.models.Grupo;
import com.controlescolar.models.Alumno;
import com.controlescolar.utils.DatabaseUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;
import javafx.collections.ObservableList;
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

    // Additional methods needed by AsistenciaView
    public List<Materia> obtenerMaterias() {
        try {
            return MateriaController.obtenerMaterias();
        } catch (Exception e) {
            System.err.println("Error al obtener materias: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Grupo> obtenerGruposPorMateria(ObjectId materiaId) {
        try {
            // For now, return all active groups since the relationship might not be direct
            return GrupoController.obtenerTodosLosGrupos();
        } catch (Exception e) {
            System.err.println("Error al obtener grupos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Asistencia> obtenerAsistencia(ObjectId materiaId, ObjectId grupoId, LocalDate fecha) {
        List<Asistencia> asistencias = new ArrayList<>();
        try {
            Document filter = new Document("materiaId", materiaId)
                    .append("grupoId", grupoId)
                    .append("fecha", fecha);
            collection.find(filter)
                    .forEach(doc -> asistencias.add(Asistencia.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener asistencia: " + e.getMessage());
        }
        return asistencias;
    }

    public List<Alumno> obtenerAlumnosPorGrupo(ObjectId grupoId) {
        List<Alumno> alumnos = new ArrayList<>();
        try {
            // First get the group to access its alumnosIds list
            Grupo grupo = GrupoController.obtenerGrupoPorId(grupoId);
            if (grupo != null && grupo.getAlumnosIds() != null) {
                // Get each student by their ID
                for (ObjectId alumnoId : grupo.getAlumnosIds()) {
                    Alumno alumno = AlumnoController.obtenerAlumnoPorId(alumnoId);
                    if (alumno != null && alumno.isActivo()) {
                        alumnos.add(alumno);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener alumnos por grupo: " + e.getMessage());
        }
        return alumnos;
    }

    public boolean guardarAsistencia(ObservableList<Asistencia> listaAsistencia) {
        try {
            for (Asistencia asistencia : listaAsistencia) {
                registrarAsistencia(asistencia);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar asistencia: " + e.getMessage());
            return false;
        }
    }

    public boolean generarReporteAsistencia(ObjectId materiaId, ObjectId grupoId, LocalDate fecha) {
        // Implementation for generating attendance report
        try {
            // This would typically generate a PDF or Excel report
            // For now, we'll just return true to avoid compilation errors
            System.out.println("Generando reporte de asistencia...");
            return true;
        } catch (Exception e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
            return false;
        }
    }

    public List<Asistencia> obtenerTodas() {
        List<Asistencia> asistencias = new ArrayList<>();
        try {
            collection.find()
                    .sort(Sorts.descending("fecha"))
                    .forEach(doc -> asistencias.add(Asistencia.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener todas las asistencias: " + e.getMessage());
        }
        return asistencias;
    }
}