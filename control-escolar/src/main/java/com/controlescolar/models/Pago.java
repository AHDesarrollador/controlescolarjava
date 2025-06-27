// Pago.java
package com.controlescolar.models;

import com.controlescolar.enums.EstadoPago;
import com.controlescolar.enums.TipoPago;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

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
    private TipoPago tipo;
    private String concepto;
    private String numeroReferencia;
    private String observaciones;
    private LocalDateTime fechaRegistro;
    private String responsableRegistro;
    
    // Additional fields for UI compatibility
    private String nombreAlumno;
    private String matriculaAlumno;
    private Alumno alumno;

    // Constructores
    public Pago() {
        this.id = new ObjectId();
        this.fechaRegistro = LocalDateTime.now();
        this.estado = EstadoPago.PENDIENTE;
        this.tipo = TipoPago.COLEGIATURA;
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
                .append("fechaPago", fechaPago != null ? 
                    Date.from(fechaPago.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null)
                .append("fechaVencimiento", fechaVencimiento != null ? 
                    Date.from(fechaVencimiento.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null)
                .append("estado", estado.name())
                .append("tipo", tipo != null ? tipo.name() : null)
                .append("concepto", concepto)
                .append("numeroReferencia", numeroReferencia)
                .append("observaciones", observaciones)
                .append("fechaRegistro", fechaRegistro != null ? 
                    Date.from(fechaRegistro.atZone(ZoneId.systemDefault()).toInstant()) : null)
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
        // Convertir fechaPago de Date a LocalDate
        java.util.Date fechaPagoDate = doc.getDate("fechaPago");
        if (fechaPagoDate != null) {
            pago.setFechaPago(fechaPagoDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate());
        }
        
        // Convertir fechaVencimiento de Date a LocalDate
        java.util.Date fechaVencimientoDate = doc.getDate("fechaVencimiento");
        if (fechaVencimientoDate != null) {
            pago.setFechaVencimiento(fechaVencimientoDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate());
        }
        
        // Parse estado with fallback
        String estadoStr = doc.getString("estado");
        EstadoPago estado = null;
        try {
            estado = EstadoPago.valueOf(estadoStr);
        } catch (IllegalArgumentException e) {
            estado = EstadoPago.fromNombre(estadoStr);
            if (estado == null) {
                estado = EstadoPago.PENDIENTE;
            }
        }
        pago.setEstado(estado);
        
        // Parse tipo with fallback
        String tipoStr = doc.getString("tipo");
        TipoPago tipo = null;
        try {
            tipo = TipoPago.valueOf(tipoStr);
        } catch (IllegalArgumentException e) {
            tipo = TipoPago.fromNombre(tipoStr);
            if (tipo == null) {
                tipo = TipoPago.COLEGIATURA;
            }
        }
        pago.setTipo(tipo);
        
        pago.setConcepto(doc.getString("concepto"));
        pago.setNumeroReferencia(doc.getString("numeroReferencia"));
        pago.setObservaciones(doc.getString("observaciones"));
        
        // Convertir fechaRegistro de Date a LocalDateTime
        java.util.Date fechaRegistroDate = doc.getDate("fechaRegistro");
        if (fechaRegistroDate != null) {
            pago.setFechaRegistro(fechaRegistroDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
        }
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
    
    public double getMonto() { return montoPagado; } // Alias for UI compatibility

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

    // Additional getters and setters for UI compatibility
    public String getNombreAlumno() { return nombreAlumno; }
    public void setNombreAlumno(String nombreAlumno) { this.nombreAlumno = nombreAlumno; }

    public String getMatriculaAlumno() { return matriculaAlumno; }
    public void setMatriculaAlumno(String matriculaAlumno) { this.matriculaAlumno = matriculaAlumno; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }
    
    public TipoPago getTipo() { return tipo; }
    public void setTipo(TipoPago tipo) { this.tipo = tipo; }
    
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    
    public String getNumeroReferencia() { return numeroReferencia; }
    public void setNumeroReferencia(String numeroReferencia) { this.numeroReferencia = numeroReferencia; }
}