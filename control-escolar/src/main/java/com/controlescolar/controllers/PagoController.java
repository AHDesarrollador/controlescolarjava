// PagoController.java
package com.controlescolar.controllers;

import com.controlescolar.models.Pago;
import com.controlescolar.enums.EstadoPago;
import com.controlescolar.utils.DatabaseUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PagoController {
    private static MongoCollection<Document> collection = DatabaseUtil.getCollection("pagos");

    public static boolean registrarPago(Pago pago) {
        try {
            collection.insertOne(pago.toDocument());
            return true;
        } catch (Exception e) {
            System.err.println("Error al registrar pago: " + e.getMessage());
            return false;
        }
    }

    public static boolean actualizarPago(Pago pago) {
        try {
            Document updateDoc = new Document()
                    .append("montoPagado", pago.getMontoPagado())
                    .append("montoOriginal", pago.getMontoOriginal())
                    .append("montoRecargo", pago.getMontoRecargo())
                    .append("montoBeca", pago.getMontoBeca())
                    .append("fechaPago", pago.getFechaPago())
                    .append("fechaVencimiento", pago.getFechaVencimiento())
                    .append("metodoPago", pago.getMetodoPago())
                    .append("estado", pago.getEstado().name())
                    .append("tipo", pago.getTipo() != null ? pago.getTipo().name() : null)
                    .append("concepto", pago.getConcepto())
                    .append("numeroReferencia", pago.getNumeroReferencia())
                    .append("periodo", pago.getPeriodo())
                    .append("observaciones", pago.getObservaciones());

            collection.updateOne(
                    Filters.eq("_id", pago.getId()),
                    new Document("$set", updateDoc)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar pago: " + e.getMessage());
            return false;
        }
    }

    public static List<Pago> obtenerPagosPorAlumno(ObjectId alumnoId) {
        List<Pago> pagos = new ArrayList<>();
        try {
            collection.find(Filters.eq("alumnoId", alumnoId))
                    .sort(Sorts.descending("fechaVencimiento"))
                    .forEach(doc -> pagos.add(Pago.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener pagos por alumno: " + e.getMessage());
        }
        return pagos;
    }

    public static List<Pago> obtenerPagosPendientes() {
        List<Pago> pagos = new ArrayList<>();
        try {
            collection.find(Filters.eq("estado", EstadoPago.PENDIENTE.toString()))
                    .sort(Sorts.ascending("fechaVencimiento"))
                    .forEach(doc -> pagos.add(Pago.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener pagos pendientes: " + e.getMessage());
        }
        return pagos;
    }

    public static List<Pago> obtenerPagosVencidos() {
        List<Pago> pagos = new ArrayList<>();
        try {
            Document filter = new Document("estado", EstadoPago.PENDIENTE.toString())
                    .append("fechaVencimiento", new Document("$lt", LocalDate.now()));

            collection.find(filter)
                    .sort(Sorts.ascending("fechaVencimiento"))
                    .forEach(doc -> pagos.add(Pago.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener pagos vencidos: " + e.getMessage());
        }
        return pagos;
    }

    public static List<Pago> obtenerPagosPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Pago> pagos = new ArrayList<>();
        try {
            Document filter = new Document("fechaPago",
                    new Document("$gte", fechaInicio).append("$lte", fechaFin));

            collection.find(filter)
                    .sort(Sorts.descending("fechaPago"))
                    .forEach(doc -> pagos.add(Pago.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener pagos por periodo: " + e.getMessage());
        }
        return pagos;
    }

    public static boolean marcarComoPagado(ObjectId pagoId, LocalDate fechaPago, String metodoPago) {
        try {
            Document updateDoc = new Document()
                    .append("estado", EstadoPago.PAGADO.toString())
                    .append("fechaPago", fechaPago)
                    .append("metodoPago", metodoPago);

            collection.updateOne(
                    Filters.eq("_id", pagoId),
                    new Document("$set", updateDoc)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al marcar pago como pagado: " + e.getMessage());
            return false;
        }
    }

    public static double calcularTotalIngresosPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            Document filter = new Document("estado", EstadoPago.PAGADO.toString())
                    .append("fechaPago", new Document("$gte", fechaInicio).append("$lte", fechaFin));

            List<Pago> pagos = new ArrayList<>();
            collection.find(filter).forEach(doc -> pagos.add(Pago.fromDocument(doc)));

            return pagos.stream().mapToDouble(Pago::getMontoPagado).sum();
        } catch (Exception e) {
            System.err.println("Error al calcular total de ingresos: " + e.getMessage());
            return 0.0;
        }
    }

    public static java.util.Map<String, Object> obtenerEstadoCuentaAlumno(ObjectId alumnoId) {
        java.util.Map<String, Object> estadoCuenta = new java.util.HashMap<>();
        try {
            List<Pago> todosPagos = obtenerPagosPorAlumno(alumnoId);
            
            double totalOriginal = todosPagos.stream().mapToDouble(Pago::getMontoOriginal).sum();
            double totalPagado = todosPagos.stream()
                    .filter(p -> p.getEstado().isCompletado())
                    .mapToDouble(Pago::getMontoPagado).sum();
            double totalPendiente = todosPagos.stream()
                    .filter(p -> !p.getEstado().isCompletado())
                    .mapToDouble(Pago::getMontoTotal).sum();
            double totalRecargos = todosPagos.stream().mapToDouble(Pago::getMontoRecargo).sum();
            double totalBecas = todosPagos.stream().mapToDouble(Pago::getMontoBeca).sum();

            int pagosVencidos = (int) todosPagos.stream()
                    .filter(p -> p.getEstado() == EstadoPago.VENCIDO || 
                                (p.getEstado() == EstadoPago.PENDIENTE && 
                                 p.getFechaVencimiento() != null && 
                                 p.getFechaVencimiento().isBefore(LocalDate.now())))
                    .count();

            estadoCuenta.put("totalOriginal", totalOriginal);
            estadoCuenta.put("totalPagado", totalPagado);
            estadoCuenta.put("totalPendiente", totalPendiente);
            estadoCuenta.put("totalRecargos", totalRecargos);
            estadoCuenta.put("totalBecas", totalBecas);
            estadoCuenta.put("saldoActual", totalPendiente);
            estadoCuenta.put("pagosVencidos", pagosVencidos);
            estadoCuenta.put("historialPagos", todosPagos);

        } catch (Exception e) {
            System.err.println("Error al obtener estado de cuenta: " + e.getMessage());
            estadoCuenta.put("error", e.getMessage());
        }
        return estadoCuenta;
    }

    public static List<Pago> obtenerAlumnosConPagosVencidos() {
        List<Pago> pagosVencidos = new ArrayList<>();
        try {
            Document filter = new Document("estado", EstadoPago.PENDIENTE.toString())
                    .append("fechaVencimiento", new Document("$lt", LocalDate.now()));

            collection.find(filter)
                    .sort(Sorts.ascending("fechaVencimiento"))
                    .forEach(doc -> pagosVencidos.add(Pago.fromDocument(doc)));

        } catch (Exception e) {
            System.err.println("Error al obtener alumnos con pagos vencidos: " + e.getMessage());
        }
        return pagosVencidos;
    }

    public static boolean aplicarRecargo(ObjectId pagoId, double montoRecargo, String motivo) {
        try {
            Document updateDoc = new Document()
                    .append("montoRecargo", montoRecargo)
                    .append("estado", EstadoPago.VENCIDO.toString())
                    .append("observaciones", motivo);

            collection.updateOne(
                    Filters.eq("_id", pagoId),
                    new Document("$set", updateDoc)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al aplicar recargo: " + e.getMessage());
            return false;
        }
    }

    public static boolean aplicarBeca(ObjectId pagoId, double montoBeca, String motivo) {
        try {
            Document updateDoc = new Document()
                    .append("montoBeca", montoBeca)
                    .append("observaciones", motivo);

            collection.updateOne(
                    Filters.eq("_id", pagoId),
                    new Document("$set", updateDoc)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al aplicar beca: " + e.getMessage());
            return false;
        }
    }

    public static boolean procesarPagoParcial(ObjectId pagoId, double montoParcial, String metodoPago, String observaciones) {
        try {
            Pago pago = obtenerPagoPorId(pagoId);
            if (pago == null) return false;

            double nuevoMontoPagado = pago.getMontoPagado() + montoParcial;
            EstadoPago nuevoEstado = (nuevoMontoPagado >= pago.getMontoTotal()) ? 
                    EstadoPago.PAGADO : EstadoPago.PARCIAL;

            Document updateDoc = new Document()
                    .append("montoPagado", nuevoMontoPagado)
                    .append("estado", nuevoEstado.toString())
                    .append("fechaPago", LocalDate.now())
                    .append("metodoPago", metodoPago)
                    .append("observaciones", observaciones);

            collection.updateOne(
                    Filters.eq("_id", pagoId),
                    new Document("$set", updateDoc)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al procesar pago parcial: " + e.getMessage());
            return false;
        }
    }

    public static Pago obtenerPagoPorId(ObjectId pagoId) {
        try {
            Document doc = collection.find(Filters.eq("_id", pagoId)).first();
            return doc != null ? Pago.fromDocument(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener pago por ID: " + e.getMessage());
            return null;
        }
    }

    public static Pago obtenerPagoPorFolio(String folio) {
        try {
            Document doc = collection.find(Filters.eq("folio", folio)).first();
            return doc != null ? Pago.fromDocument(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener pago por folio: " + e.getMessage());
            return null;
        }
    }

    public static java.util.Map<String, Object> generarReporteIngresos(LocalDate fechaInicio, LocalDate fechaFin) {
        java.util.Map<String, Object> reporte = new java.util.HashMap<>();
        try {
            List<Pago> pagosPeriodo = obtenerPagosPorPeriodo(fechaInicio, fechaFin);
            
            double totalIngresos = pagosPeriodo.stream()
                    .filter(p -> p.getEstado() == EstadoPago.PAGADO)
                    .mapToDouble(Pago::getMontoPagado).sum();
            
            java.util.Map<String, Double> ingresosPorMetodo = pagosPeriodo.stream()
                    .filter(p -> p.getEstado() == EstadoPago.PAGADO)
                    .collect(java.util.stream.Collectors.groupingBy(
                            Pago::getMetodoPago,
                            java.util.stream.Collectors.summingDouble(Pago::getMontoPagado)
                    ));

            java.util.Map<String, Long> pagosPorEstado = pagosPeriodo.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            p -> p.getEstado().getNombre(),
                            java.util.stream.Collectors.counting()
                    ));

            reporte.put("totalIngresos", totalIngresos);
            reporte.put("ingresosPorMetodo", ingresosPorMetodo);
            reporte.put("pagosPorEstado", pagosPorEstado);
            reporte.put("totalPagos", pagosPeriodo.size());
            reporte.put("pagosCompletados", pagosPeriodo.stream().mapToInt(p -> p.getEstado().isCompletado() ? 1 : 0).sum());

        } catch (Exception e) {
            System.err.println("Error al generar reporte de ingresos: " + e.getMessage());
            reporte.put("error", e.getMessage());
        }
        return reporte;
    }

    public static List<java.util.Map<String, Object>> obtenerTop5AlumnosConMayorDeuda() {
        List<java.util.Map<String, Object>> ranking = new ArrayList<>();
        try {
            java.util.Map<ObjectId, Double> deudasPorAlumno = new java.util.HashMap<>();
            
            List<Pago> pagosPendientes = collection.find(
                Filters.in("estado", EstadoPago.PENDIENTE.toString(), EstadoPago.PARCIAL.toString(), EstadoPago.VENCIDO.toString())
            ).map(Pago::fromDocument).into(new ArrayList<>());

            for (Pago pago : pagosPendientes) {
                deudasPorAlumno.merge(pago.getAlumnoId(), pago.getSaldoPendiente(), Double::sum);
            }

            ranking = deudasPorAlumno.entrySet().stream()
                    .sorted(java.util.Map.Entry.<ObjectId, Double>comparingByValue().reversed())
                    .limit(5)
                    .map(entry -> {
                        java.util.Map<String, Object> item = new java.util.HashMap<>();
                        item.put("alumnoId", entry.getKey());
                        item.put("deuda", entry.getValue());
                        return item;
                    })
                    .collect(java.util.stream.Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error al obtener top alumnos con mayor deuda: " + e.getMessage());
        }
        return ranking;
    }

    public static boolean eliminarPago(ObjectId id) {
        try {
            collection.deleteOne(Filters.eq("_id", id));
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar pago: " + e.getMessage());
            return false;
        }
    }

    // Additional methods for UI compatibility
    public List<Pago> obtenerTodos() {
        return obtenerPagos();
    }

    public static List<Pago> obtenerPagos() {
        List<Pago> pagos = new ArrayList<>();
        try {
            collection.find().forEach(doc -> pagos.add(Pago.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener pagos: " + e.getMessage());
        }
        return pagos;
    }

    public static boolean generarPago(ObjectId alumnoId, String concepto, double monto, String observaciones) {
        try {
            Pago pago = new Pago();
            pago.setAlumnoId(alumnoId);
            pago.setConcepto(concepto);
            pago.setMontoOriginal(monto);
            pago.setObservaciones(observaciones);
            pago.setFechaVencimiento(LocalDate.now().plusDays(30)); // Vencimiento en 30 días
            pago.setEstado(EstadoPago.PENDIENTE);
            
            return registrarPago(pago);
        } catch (Exception e) {
            System.err.println("Error al generar pago: " + e.getMessage());
            return false;
        }
    }
}