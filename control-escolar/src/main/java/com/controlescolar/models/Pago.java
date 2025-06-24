// Pago.java
package com.controlescolar.models;

import com.controlescolar.enums.EstadoPago;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Pago {
    private ObjectId id;
    private ObjectId alumnoId;
    private String folio;
    private double montoPagado;
    private double montoOriginal;
    private double montoRecargo;
    private double montoBeca;
    private String periodo;
    private String metodoPago;
    private LocalDate fechaPago;
    private LocalDate fechaVencimiento;
    private EstadoPago estado;
    private String observaciones;
    private LocalDateTime fechaRegistro;
    private String responsableRegistro;

    // Constructores
    public Pago() {
        this.id = new ObjectId();
        this.fechaRegistro = LocalDateTime.now();
        this.estado = EstadoPago.PENDIENTE;
    }

    public Pago(ObjectId alumnoId, double montoOriginal, String periodo, LocalDate fechaVencimiento) {
        this();
        this.alumnoId = alumnoId;
        this.montoOriginal = montoOriginal;
        this.periodo = periodo;
        this.fechaVencimiento = fechaVencimiento;
        this.folio = generarFolio();
    }

    // Método para generar folio único
    private String generarFolio() {
        return "PAG" + System.currentTimeMillis();
    }

    // Métodos para conversión Document
    public Document toDocument() {
        return new Document("_id", id)
                .append("alumnoId", alumnoId)
                .append("folio", folio)
                .append("montoPagado", montoPagado)
                .append("montoOriginal", montoOriginal)
                .append("montoRecargo", montoRecargo)
                .append("montoBeca", montoBeca)
                .append("periodo", periodo)
                .append("metodoPago", metodoPago)
                .append("fechaPago", fechaPago)
                .append("fechaVencimiento", fechaVencimiento)
                .append("estado", estado.toString())
                .append("observaciones", observaciones)
                .append("fechaRegistro", fechaRegistro)
                .append("responsableRegistro", responsableRegistro);
    }

    public static Pago fromDocument(Document doc) {
        Pago pago = new Pago();
        pago.setId(doc.getObjectId("_id"));
        pago.setAlumnoId(doc.getObjectId("alumnoId"));
        pago.setFolio(doc.getString("folio"));
        pago.setMontoPagado(doc.getDouble("montoPagado"));
        pago.setMontoOriginal(doc.getDouble("montoOriginal"));
        pago.setMontoRecargo(doc.getDouble("montoRecargo"));
        pago.setMontoBeca(doc.getDouble("montoBeca"));
        pago.setPeriodo(doc.getString("periodo"));
        pago.setMetodoPago(doc.getString("metodoPago"));
        pago.setFechaPago(doc.get("fechaPago", LocalDate.class));
        pago.setFechaVencimiento(doc.get("fechaVencimiento", LocalDate.class));
        pago.setEstado(EstadoPago.valueOf(doc.getString("estado")));
        pago.setObservaciones(doc.getString("observaciones"));
        pago.setFechaRegistro(doc.get("fechaRegistro", LocalDateTime.class));
        pago.setResponsableRegistro(doc.getString("responsableRegistro"));
        return pago;
    }

    // Método para calcular el monto total a pagar
    public double getMontoTotal() {
        return montoOriginal + montoRecargo - montoBeca;
    }

    // Método para calcular el saldo pendiente
    public double getSaldoPendiente() {
        return getMontoTotal() - montoPagado;
    }

    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getAlumnoId() { return alumnoId; }
    public void setAlumnoId(ObjectId alumnoId) { this.alumnoId = alumnoId; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public double getMontoPagado() { return montoPagado; }
    public void setMontoPagado(double montoPagado) { this.montoPagado = montoPagado; }

    public double getMontoOriginal() { return montoOriginal; }
    public void setMontoOriginal(double montoOriginal) { this.montoOriginal = montoOriginal; }

    public double getMontoRecargo() { return montoRecargo; }
    public void setMontoRecargo(double montoRecargo) { this.montoRecargo = montoRecargo; }

    public double getMontoBeca() { return montoBeca; }
    public void setMontoBeca(double montoBeca) { this.montoBeca = montoBeca; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public EstadoPago getEstado() { return estado; }
    public void setEstado(EstadoPago estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getResponsableRegistro() { return responsableRegistro; }
    public void setResponsableRegistro(String responsableRegistro) { this.responsableRegistro = responsableRegistro; }
}