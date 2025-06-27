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

            double sumaCalificacionesPonderadas = 0.0;
            double sumaPonderaciones = 0.0;

            for (Calificacion cal : calificaciones) {
                double ponderacion = cal.getPonderacion() > 0 ? cal.getPonderacion() : cal.getTipo().getPesoDefault();
                sumaCalificacionesPonderadas += cal.getCalificacion() * (ponderacion / 100.0);
                sumaPonderaciones += ponderacion / 100.0;
            }

            if (sumaPonderaciones == 0) return 0.0;
            
            double promedio = sumaCalificacionesPonderadas / sumaPonderaciones;
            return Math.round(promedio * 100.0) / 100.0;
        } catch (Exception e) {
            System.err.println("Error al calcular promedio: " + e.getMessage());
            return 0.0;
        }
    }

    public static double calcularPromedioGeneralAlumno(ObjectId alumnoId) {
        try {
            List<Calificacion> todasCalificaciones = obtenerCalificacionesPorAlumno(alumnoId);
            if (todasCalificaciones.isEmpty()) return 0.0;

            // Agrupar por materia y calcular promedio por materia
            java.util.Map<ObjectId, List<Calificacion>> calificacionesPorMateria = new java.util.HashMap<>();
            for (Calificacion cal : todasCalificaciones) {
                calificacionesPorMateria.computeIfAbsent(cal.getMateriaId(), k -> new ArrayList<>()).add(cal);
            }

            double sumaPromedios = 0.0;
            int contadorMaterias = 0;

            for (java.util.Map.Entry<ObjectId, List<Calificacion>> entry : calificacionesPorMateria.entrySet()) {
                double promedioMateria = calcularPromedioAlumno(alumnoId, entry.getKey());
                if (promedioMateria > 0) {
                    sumaPromedios += promedioMateria;
                    contadorMaterias++;
                }
            }

            return contadorMaterias > 0 ? Math.round((sumaPromedios / contadorMaterias) * 100.0) / 100.0 : 0.0;
        } catch (Exception e) {
            System.err.println("Error al calcular promedio general: " + e.getMessage());
            return 0.0;
        }
    }

    public static List<Calificacion> obtenerCalificacionesPorPeriodo(String periodo) {
        List<Calificacion> calificaciones = new ArrayList<>();
        try {
            collection.find(Filters.eq("periodo", periodo))
                    .sort(Sorts.descending("fechaRegistro"))
                    .forEach(doc -> calificaciones.add(Calificacion.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener calificaciones por período: " + e.getMessage());
        }
        return calificaciones;
    }

    public static List<Calificacion> obtenerCalificacionesPorTipo(TipoCalificacion tipo) {
        List<Calificacion> calificaciones = new ArrayList<>();
        try {
            collection.find(Filters.eq("tipo", tipo.toString()))
                    .sort(Sorts.descending("fechaRegistro"))
                    .forEach(doc -> calificaciones.add(Calificacion.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener calificaciones por tipo: " + e.getMessage());
        }
        return calificaciones;
    }

    public static boolean validarCalificacion(double calificacion, TipoCalificacion tipo) {
        // Validar rango (0-10)
        if (calificacion < 0 || calificacion > 10) {
            return false;
        }
        
        // Validar calificación mínima según tipo si requiere aprobación
        if (tipo.isRequiereAprobacion() && calificacion < tipo.getCalificacionMinima()) {
            System.out.println("Advertencia: Calificación por debajo del mínimo requerido para " + tipo.getNombre());
        }
        
        return true;
    }

    public static String determinarEstadoAprobacion(double promedio) {
        if (promedio >= 8.0) return "Excelente";
        else if (promedio >= 7.0) return "Bueno";
        else if (promedio >= 6.0) return "Aprobado";
        else return "Reprobado";
    }

    public static List<java.util.Map<String, Object>> obtenerHistorialAcademico(ObjectId alumnoId) {
        List<java.util.Map<String, Object>> historial = new ArrayList<>();
        try {
            List<Calificacion> calificaciones = obtenerCalificacionesPorAlumno(alumnoId);
            
            // Agrupar por materia y período
            java.util.Map<String, java.util.Map<ObjectId, List<Calificacion>>> historialPorPeriodo = new java.util.HashMap<>();
            
            for (Calificacion cal : calificaciones) {
                String periodo = cal.getPeriodo() != null ? cal.getPeriodo() : "Sin período";
                historialPorPeriodo.computeIfAbsent(periodo, k -> new java.util.HashMap<>())
                        .computeIfAbsent(cal.getMateriaId(), k -> new ArrayList<>()).add(cal);
            }

            for (java.util.Map.Entry<String, java.util.Map<ObjectId, List<Calificacion>>> periodoEntry : historialPorPeriodo.entrySet()) {
                for (java.util.Map.Entry<ObjectId, List<Calificacion>> materiaEntry : periodoEntry.getValue().entrySet()) {
                    java.util.Map<String, Object> registro = new java.util.HashMap<>();
                    registro.put("periodo", periodoEntry.getKey());
                    registro.put("materiaId", materiaEntry.getKey());
                    
                    double promedio = calcularPromedioAlumno(alumnoId, materiaEntry.getKey());
                    registro.put("promedio", promedio);
                    registro.put("estado", determinarEstadoAprobacion(promedio));
                    registro.put("calificaciones", materiaEntry.getValue());
                    
                    historial.add(registro);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener historial académico: " + e.getMessage());
        }
        return historial;
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

    // Additional methods for UI compatibility
    public static List<Calificacion> obtenerTodas() {
        List<Calificacion> calificaciones = new ArrayList<>();
        try {
            collection.find().sort(Sorts.descending("fechaRegistro")).forEach(doc -> calificaciones.add(Calificacion.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener todas las calificaciones: " + e.getMessage());
        }
        return calificaciones;
    }
}