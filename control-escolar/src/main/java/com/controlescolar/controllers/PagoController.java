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
                    .append("monto", pago.getMonto())
                    .append("fechaPago", pago.getFechaPago())
                    .append("metodoPago", pago.getMetodoPago())
                    .append("estado", pago.getEstado().toString())
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

            return pagos.stream().mapToDouble(Pago::getMonto).sum();
        } catch (Exception e) {
            System.err.println("Error al calcular total de ingresos: " + e.getMessage());
            return 0.0;
        }
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
}