// ReportsController.java
package com.controlescolar.controllers;

import com.controlescolar.models.*;
import com.controlescolar.enums.TipoCalificacion;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ReportsController {

    // Reportes de Calificaciones
    public static Map<String, Object> obtenerReporteCalificacionesPorMateria(ObjectId materiaId) {
        Map<String, Object> reporte = new HashMap<>();
        try {
            List<Calificacion> calificaciones = CalificacionController.obtenerCalificacionesPorMateria(materiaId);
            
            if (calificaciones.isEmpty()) {
                reporte.put("error", "No hay calificaciones para esta materia");
                return reporte;
            }

            // Estadísticas generales
            double promedioGeneral = calificaciones.stream()
                    .mapToDouble(Calificacion::getCalificacion)
                    .average().orElse(0.0);
            
            double calificacionMaxima = calificaciones.stream()
                    .mapToDouble(Calificacion::getCalificacion)
                    .max().orElse(0.0);
            
            double calificacionMinima = calificaciones.stream()
                    .mapToDouble(Calificacion::getCalificacion)
                    .min().orElse(0.0);

            // Distribución por rangos
            Map<String, Integer> distribucion = new HashMap<>();
            distribucion.put("Excelente (9-10)", 0);
            distribucion.put("Bueno (8-8.9)", 0);
            distribucion.put("Regular (7-7.9)", 0);
            distribucion.put("Suficiente (6-6.9)", 0);
            distribucion.put("Insuficiente (0-5.9)", 0);

            for (Calificacion cal : calificaciones) {
                double calif = cal.getCalificacion();
                if (calif >= 9.0) distribucion.put("Excelente (9-10)", distribucion.get("Excelente (9-10)") + 1);
                else if (calif >= 8.0) distribucion.put("Bueno (8-8.9)", distribucion.get("Bueno (8-8.9)") + 1);
                else if (calif >= 7.0) distribucion.put("Regular (7-7.9)", distribucion.get("Regular (7-7.9)") + 1);
                else if (calif >= 6.0) distribucion.put("Suficiente (6-6.9)", distribucion.get("Suficiente (6-6.9)") + 1);
                else distribucion.put("Insuficiente (0-5.9)", distribucion.get("Insuficiente (0-5.9)") + 1);
            }

            reporte.put("totalCalificaciones", calificaciones.size());
            reporte.put("promedioGeneral", Math.round(promedioGeneral * 100.0) / 100.0);
            reporte.put("calificacionMaxima", calificacionMaxima);
            reporte.put("calificacionMinima", calificacionMinima);
            reporte.put("distribucion", distribucion);
            reporte.put("aprobados", calificaciones.stream().mapToInt(c -> c.getCalificacion() >= 6.0 ? 1 : 0).sum());
            reporte.put("reprobados", calificaciones.stream().mapToInt(c -> c.getCalificacion() < 6.0 ? 1 : 0).sum());

        } catch (Exception e) {
            System.err.println("Error al generar reporte de calificaciones por materia: " + e.getMessage());
            reporte.put("error", e.getMessage());
        }
        return reporte;
    }

    public static Map<String, Object> obtenerReporteDesempenoPorGrupo(ObjectId grupoId) {
        Map<String, Object> reporte = new HashMap<>();
        try {
            List<Alumno> alumnos = GrupoController.obtenerAlumnosDeGrupo(grupoId);
            List<Materia> materias = GrupoController.obtenerMateriasDeGrupo(grupoId);

            if (alumnos.isEmpty()) {
                reporte.put("error", "No hay alumnos en este grupo");
                return reporte;
            }

            Map<ObjectId, Double> promediosPorAlumno = new HashMap<>();
            Map<ObjectId, Double> promediosPorMateria = new HashMap<>();
            
            // Calcular promedios por alumno
            for (Alumno alumno : alumnos) {
                double promedioGeneral = CalificacionController.calcularPromedioGeneralAlumno(alumno.getId());
                promediosPorAlumno.put(alumno.getId(), promedioGeneral);
            }

            // Calcular promedios por materia
            for (Materia materia : materias) {
                List<Calificacion> calificacionesMateria = CalificacionController.obtenerCalificacionesPorMateria(materia.getId());
                double promedio = calificacionesMateria.stream()
                        .mapToDouble(Calificacion::getCalificacion)
                        .average().orElse(0.0);
                promediosPorMateria.put(materia.getId(), promedio);
            }

            // Estadísticas del grupo
            double promedioGrupo = promediosPorAlumno.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average().orElse(0.0);

            reporte.put("totalAlumnos", alumnos.size());
            reporte.put("totalMaterias", materias.size());
            reporte.put("promedioGrupo", Math.round(promedioGrupo * 100.0) / 100.0);
            reporte.put("promediosPorAlumno", promediosPorAlumno);
            reporte.put("promediosPorMateria", promediosPorMateria);
            reporte.put("alumnosAprobados", promediosPorAlumno.values().stream().mapToInt(p -> p >= 6.0 ? 1 : 0).sum());
            reporte.put("alumnosReprobados", promediosPorAlumno.values().stream().mapToInt(p -> p < 6.0 ? 1 : 0).sum());

        } catch (Exception e) {
            System.err.println("Error al generar reporte de desempeño por grupo: " + e.getMessage());
            reporte.put("error", e.getMessage());
        }
        return reporte;
    }

    public static Map<String, Object> obtenerComparativaPeriodos(List<String> periodos) {
        Map<String, Object> reporte = new HashMap<>();
        try {
            Map<String, Double> promediosPorPeriodo = new HashMap<>();
            Map<String, Integer> estudiantesPorPeriodo = new HashMap<>();

            for (String periodo : periodos) {
                List<Calificacion> calificacionesPeriodo = CalificacionController.obtenerCalificacionesPorPeriodo(periodo);
                
                if (!calificacionesPeriodo.isEmpty()) {
                    double promedio = calificacionesPeriodo.stream()
                            .mapToDouble(Calificacion::getCalificacion)
                            .average().orElse(0.0);
                    
                    Set<ObjectId> estudiantesUnicos = calificacionesPeriodo.stream()
                            .map(Calificacion::getAlumnoId)
                            .collect(Collectors.toSet());

                    promediosPorPeriodo.put(periodo, Math.round(promedio * 100.0) / 100.0);
                    estudiantesPorPeriodo.put(periodo, estudiantesUnicos.size());
                }
            }

            reporte.put("promediosPorPeriodo", promediosPorPeriodo);
            reporte.put("estudiantesPorPeriodo", estudiantesPorPeriodo);

        } catch (Exception e) {
            System.err.println("Error al generar comparativa de períodos: " + e.getMessage());
            reporte.put("error", e.getMessage());
        }
        return reporte;
    }

    public static Map<String, Object> obtenerProgresoIndividualAlumno(ObjectId alumnoId) {
        Map<String, Object> reporte = new HashMap<>();
        try {
            List<Map<String, Object>> historialAcademico = CalificacionController.obtenerHistorialAcademico(alumnoId);
            List<Calificacion> todasCalificaciones = CalificacionController.obtenerCalificacionesPorAlumno(alumnoId);

            // Evolución temporal de calificaciones
            Map<String, Double> evolucionTemporal = new LinkedHashMap<>();
            Map<String, List<Calificacion>> calificacionesPorPeriodo = todasCalificaciones.stream()
                    .collect(Collectors.groupingBy(c -> c.getPeriodo() != null ? c.getPeriodo() : "Sin período"));

            for (Map.Entry<String, List<Calificacion>> entry : calificacionesPorPeriodo.entrySet()) {
                double promedioPeriodo = entry.getValue().stream()
                        .mapToDouble(Calificacion::getCalificacion)
                        .average().orElse(0.0);
                evolucionTemporal.put(entry.getKey(), Math.round(promedioPeriodo * 100.0) / 100.0);
            }

            // Materias con mejor y peor desempeño
            Map<ObjectId, Double> promediosPorMateria = new HashMap<>();
            Map<ObjectId, List<Calificacion>> calificacionesPorMateria = todasCalificaciones.stream()
                    .collect(Collectors.groupingBy(Calificacion::getMateriaId));

            for (Map.Entry<ObjectId, List<Calificacion>> entry : calificacionesPorMateria.entrySet()) {
                double promedio = CalificacionController.calcularPromedioAlumno(alumnoId, entry.getKey());
                promediosPorMateria.put(entry.getKey(), promedio);
            }

            reporte.put("historialAcademico", historialAcademico);
            reporte.put("evolucionTemporal", evolucionTemporal);
            reporte.put("promediosPorMateria", promediosPorMateria);
            reporte.put("promedioGeneral", CalificacionController.calcularPromedioGeneralAlumno(alumnoId));
            reporte.put("totalCalificaciones", todasCalificaciones.size());

        } catch (Exception e) {
            System.err.println("Error al generar progreso individual: " + e.getMessage());
            reporte.put("error", e.getMessage());
        }
        return reporte;
    }

    // Generación de gráficas
    public static JFreeChart crearGraficaBarrasPromediosPorMateria(Map<ObjectId, Double> promedios) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<ObjectId, Double> entry : promedios.entrySet()) {
            // Obtener nombre de la materia
            Materia materia = MateriaController.obtenerMateriaPorId(entry.getKey());
            String nombreMateria = materia != null ? materia.getNombre() : "Materia " + entry.getKey().toString().substring(0, 8);
            dataset.addValue(entry.getValue(), "Promedio", nombreMateria);
        }

        return ChartFactory.createBarChart(
                "Promedios por Materia",
                "Materias",
                "Promedio",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }

    public static JFreeChart crearGraficaPastelDistribucionCalificaciones(Map<String, Integer> distribucion) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        for (Map.Entry<String, Integer> entry : distribucion.entrySet()) {
            if (entry.getValue() > 0) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }
        }

        return ChartFactory.createPieChart(
                "Distribución de Calificaciones",
                dataset,
                true,
                true,
                false
        );
    }

    public static JFreeChart crearGraficaEvolucionTemporal(Map<String, Double> evolucion) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Double> entry : evolucion.entrySet()) {
            dataset.addValue(entry.getValue(), "Promedio", entry.getKey());
        }

        return ChartFactory.createLineChart(
                "Evolución Temporal de Calificaciones",
                "Período",
                "Promedio",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }

    public static JFreeChart crearGraficaComparativaPeriodos(Map<String, Double> promediosPorPeriodo) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Double> entry : promediosPorPeriodo.entrySet()) {
            dataset.addValue(entry.getValue(), "Promedio General", entry.getKey());
        }

        return ChartFactory.createBarChart(
                "Comparativa de Períodos",
                "Período",
                "Promedio",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }

    // Filtros para reportes
    public static class FiltroReporte {
        private List<ObjectId> gruposIds;
        private List<ObjectId> materiasIds;
        private List<ObjectId> profesoresIds;
        private LocalDateTime fechaInicio;
        private LocalDateTime fechaFin;
        private List<String> periodos;
        private List<TipoCalificacion> tiposCalificacion;

        // Getters y setters
        public List<ObjectId> getGruposIds() { return gruposIds; }
        public void setGruposIds(List<ObjectId> gruposIds) { this.gruposIds = gruposIds; }

        public List<ObjectId> getMateriasIds() { return materiasIds; }
        public void setMateriasIds(List<ObjectId> materiasIds) { this.materiasIds = materiasIds; }

        public List<ObjectId> getProfesoresIds() { return profesoresIds; }
        public void setProfesoresIds(List<ObjectId> profesoresIds) { this.profesoresIds = profesoresIds; }

        public LocalDateTime getFechaInicio() { return fechaInicio; }
        public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

        public LocalDateTime getFechaFin() { return fechaFin; }
        public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

        public List<String> getPeriodos() { return periodos; }
        public void setPeriodos(List<String> periodos) { this.periodos = periodos; }

        public List<TipoCalificacion> getTiposCalificacion() { return tiposCalificacion; }
        public void setTiposCalificacion(List<TipoCalificacion> tiposCalificacion) { this.tiposCalificacion = tiposCalificacion; }
    }

    public static Map<String, Object> generarReportePersonalizado(FiltroReporte filtro) {
        Map<String, Object> reporte = new HashMap<>();
        try {
            // Implementar lógica de filtrado personalizado
            // Este método permitiría generar reportes con múltiples filtros combinados
            reporte.put("mensaje", "Reporte personalizado en desarrollo");
        } catch (Exception e) {
            System.err.println("Error al generar reporte personalizado: " + e.getMessage());
            reporte.put("error", e.getMessage());
        }
        return reporte;
    }
}