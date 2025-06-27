//MisPagosView.java
package com.controlescolar.views;

import com.controlescolar.controllers.PagoController;
import com.controlescolar.controllers.AlumnoController;
import com.controlescolar.models.Pago;
import com.controlescolar.models.Alumno;
import com.controlescolar.models.Usuario;
import com.controlescolar.enums.EstadoPago;
import com.controlescolar.enums.TipoPago;
import com.controlescolar.utils.PDFGenerator;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.awt.Desktop;

public class MisPagosView extends Application {
    private Stage stage;
    private Usuario usuarioActual;
    private Alumno alumnoActual;
    private TableView<Pago> tablaPagos;
    private ObservableList<Pago> listaPagos;
    private ComboBox<TipoPago> filtroTipoCombo;
    private ComboBox<EstadoPago> filtroEstadoCombo;
    private DatePicker fechaDesde;
    private DatePicker fechaHasta;
    
    // Labels para estadísticas
    private Label totalPagadoLabel;
    private Label totalPendienteLabel;
    private Label totalPagosLabel;
    private Label estadoCuentaLabel;

    public MisPagosView(Usuario usuario) {
        this.usuarioActual = usuario;
        this.listaPagos = FXCollections.observableArrayList();
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Mis Pagos - Sistema Control Escolar");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        root.setTop(createHeader());
        
        // Centro - contenido principal
        VBox centerContent = new VBox(15);
        centerContent.setPadding(new Insets(20));
        
        // Estadísticas de pagos
        centerContent.getChildren().add(createEstadisticasPanel());
        
        // Filtros
        centerContent.getChildren().add(createFiltrosPanel());
        
        // Tabla de pagos
        centerContent.getChildren().add(createTablaPagos());
        
        root.setCenter(centerContent);
        
        // Footer
        root.setBottom(createFooter());
        
        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.show();
        
        // Cargar datos
        cargarDatosAlumno();
        cargarPagos();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #f39c12;");
        
        Label titulo = new Label("💰 Mis Pagos");
        titulo.setTextFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        Label subtitulo = new Label("Consulta tu historial de pagos y estado de cuenta");
        subtitulo.setTextFill(Color.WHITE);
        subtitulo.setFont(Font.font("Arial", 14));
        
        header.getChildren().addAll(titulo, subtitulo);
        return header;
    }

    private HBox createEstadisticasPanel() {
        HBox estadisticas = new HBox(20);
        estadisticas.setAlignment(Pos.CENTER);
        estadisticas.setPadding(new Insets(20));
        estadisticas.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Total Pagado
        VBox pagadoBox = createStatCard("💵 Total Pagado", "$0.00", Color.web("#27ae60"));
        totalPagadoLabel = (Label) ((VBox) pagadoBox.getChildren().get(0)).getChildren().get(1);
        
        // Total Pendiente
        VBox pendienteBox = createStatCard("⏳ Total Pendiente", "$0.00", Color.web("#e74c3c"));
        totalPendienteLabel = (Label) ((VBox) pendienteBox.getChildren().get(0)).getChildren().get(1);
        
        // Total de Pagos
        VBox totalBox = createStatCard("📊 Total Pagos", "0", Color.web("#3498db"));
        totalPagosLabel = (Label) ((VBox) totalBox.getChildren().get(0)).getChildren().get(1);
        
        // Estado de Cuenta
        VBox estadoBox = createStatCard("🎯 Estado Cuenta", "Al día", Color.web("#9b59b6"));
        estadoCuentaLabel = (Label) ((VBox) estadoBox.getChildren().get(0)).getChildren().get(1);
        
        estadisticas.getChildren().addAll(pagadoBox, pendienteBox, totalBox, estadoBox);
        return estadisticas;
    }

    private VBox createStatCard(String titulo, String valor, Color color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: " + toHex(color) + "; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        card.setPrefWidth(200);
        
        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER);
        
        Label tituloLabel = new Label(titulo);
        tituloLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        tituloLabel.setTextFill(Color.GRAY);
        
        Label valorLabel = new Label(valor);
        valorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        valorLabel.setTextFill(color);
        
        content.getChildren().addAll(tituloLabel, valorLabel);
        card.getChildren().add(content);
        
        return card;
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private HBox createFiltrosPanel() {
        HBox filtros = new HBox(15);
        filtros.setAlignment(Pos.CENTER_LEFT);
        filtros.setPadding(new Insets(15));
        filtros.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8px;");
        
        // Filtro por tipo de pago
        Label tipoLabel = new Label("Tipo de Pago:");
        tipoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        filtroTipoCombo = new ComboBox<>();
        filtroTipoCombo.getItems().add(null); // "Todos"
        filtroTipoCombo.getItems().addAll(TipoPago.values());
        filtroTipoCombo.setPrefWidth(150);
        filtroTipoCombo.setConverter(new javafx.util.StringConverter<TipoPago>() {
            @Override
            public String toString(TipoPago tipo) {
                return tipo == null ? "Todos los tipos" : tipo.toString();
            }
            @Override
            public TipoPago fromString(String string) {
                return null;
            }
        });
        filtroTipoCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrarPagos());
        
        // Filtro por estado
        Label estadoLabel = new Label("Estado:");
        estadoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        filtroEstadoCombo = new ComboBox<>();
        filtroEstadoCombo.getItems().add(null); // "Todos"
        filtroEstadoCombo.getItems().addAll(EstadoPago.values());
        filtroEstadoCombo.setConverter(new javafx.util.StringConverter<EstadoPago>() {
            @Override
            public String toString(EstadoPago estado) {
                return estado == null ? "Todos los estados" : estado.toString();
            }
            @Override
            public EstadoPago fromString(String string) {
                return null;
            }
        });
        filtroEstadoCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrarPagos());
        
        // Filtro por fecha desde
        Label fechaDesdeLabel = new Label("Desde:");
        fechaDesdeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        fechaDesde = new DatePicker();
        fechaDesde.setValue(LocalDate.now().minusMonths(3)); // Últimos 3 meses por defecto
        fechaDesde.valueProperty().addListener((obs, oldVal, newVal) -> filtrarPagos());
        
        // Filtro por fecha hasta
        Label fechaHastaLabel = new Label("Hasta:");
        fechaHastaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        fechaHasta = new DatePicker();
        fechaHasta.setValue(LocalDate.now());
        fechaHasta.valueProperty().addListener((obs, oldVal, newVal) -> filtrarPagos());
        
        // Botón actualizar
        Button btnActualizar = new Button("🔄 Actualizar");
        btnActualizar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
        btnActualizar.setOnAction(e -> cargarPagos());
        
        // Botón limpiar filtros
        Button btnLimpiar = new Button("🗑️ Limpiar Filtros");
        btnLimpiar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        btnLimpiar.setOnAction(e -> limpiarFiltros());
        
        filtros.getChildren().addAll(tipoLabel, filtroTipoCombo, estadoLabel, filtroEstadoCombo,
                fechaDesdeLabel, fechaDesde, fechaHastaLabel, fechaHasta, btnActualizar, btnLimpiar);
        return filtros;
    }

    private VBox createTablaPagos() {
        VBox container = new VBox(10);
        
        Label tituloTabla = new Label("📋 Historial de Pagos");
        tituloTabla.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        tablaPagos = new TableView<>();
        tablaPagos.setItems(listaPagos);
        
        // Columnas
        TableColumn<Pago, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaPago();
            return new javafx.beans.property.SimpleStringProperty(
                fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""
            );
        });
        colFecha.setPrefWidth(100);
        
        TableColumn<Pago, String> colConcepto = new TableColumn<>("Concepto");
        colConcepto.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        colConcepto.setPrefWidth(200);
        
        TableColumn<Pago, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(cellData -> {
            TipoPago tipo = cellData.getValue().getTipo();
            return new javafx.beans.property.SimpleStringProperty(tipo != null ? tipo.toString() : "");
        });
        colTipo.setPrefWidth(120);
        
        TableColumn<Pago, Double> colMonto = new TableColumn<>("Monto Total");
        colMonto.setCellValueFactory(cellData -> {
            Pago pago = cellData.getValue();
            return new javafx.beans.property.SimpleDoubleProperty(pago.getMontoTotal()).asObject();
        });
        colMonto.setCellFactory(column -> new TableCell<Pago, Double>() {
            @Override
            protected void updateItem(Double monto, boolean empty) {
                super.updateItem(monto, empty);
                if (empty || monto == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", monto));
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        colMonto.setPrefWidth(120);
        
        TableColumn<Pago, Double> colSaldoPendiente = new TableColumn<>("Saldo Pendiente");
        colSaldoPendiente.setCellValueFactory(cellData -> {
            Pago pago = cellData.getValue();
            double saldo = pago.getSaldoPendiente();
            return new javafx.beans.property.SimpleDoubleProperty(saldo).asObject();
        });
        colSaldoPendiente.setCellFactory(column -> new TableCell<Pago, Double>() {
            @Override
            protected void updateItem(Double saldo, boolean empty) {
                super.updateItem(saldo, empty);
                if (empty || saldo == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("$%.2f", saldo));
                    if (saldo > 0) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Rojo para deudas
                    } else {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Verde para pagado
                    }
                }
            }
        });
        colSaldoPendiente.setPrefWidth(120);
        
        TableColumn<Pago, Double> colMontoPagado = new TableColumn<>("Monto Pagado");
        colMontoPagado.setCellValueFactory(cellData -> {
            Pago pago = cellData.getValue();
            return new javafx.beans.property.SimpleDoubleProperty(pago.getMontoPagado()).asObject();
        });
        colMontoPagado.setCellFactory(column -> new TableCell<Pago, Double>() {
            @Override
            protected void updateItem(Double montoPagado, boolean empty) {
                super.updateItem(montoPagado, empty);
                if (empty || montoPagado == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", montoPagado));
                    if (montoPagado > 0) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Verde para pagado
                    } else {
                        setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;"); // Gris para no pagado
                    }
                }
            }
        });
        colMontoPagado.setPrefWidth(120);
        
        TableColumn<Pago, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> {
            EstadoPago estado = cellData.getValue().getEstado();
            return new javafx.beans.property.SimpleStringProperty(estado != null ? estado.toString() : "");
        });
        colEstado.setCellFactory(column -> new TableCell<Pago, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    // Colorear según el estado
                    switch (estado.toLowerCase()) {
                        case "pagado":
                        case "completado":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Verde
                            break;
                        case "pendiente":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); // Naranja
                            break;
                        case "vencido":
                        case "cancelado":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Rojo
                            break;
                        case "parcial":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;"); // Azul
                            break;
                        default:
                            setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;"); // Gris
                            break;
                    }
                }
            }
        });
        colEstado.setPrefWidth(100);
        
        TableColumn<Pago, String> colFechaVencimiento = new TableColumn<>("Vencimiento");
        colFechaVencimiento.setCellValueFactory(cellData -> {
            LocalDate fechaVenc = cellData.getValue().getFechaVencimiento();
            return new javafx.beans.property.SimpleStringProperty(
                fechaVenc != null ? fechaVenc.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""
            );
        });
        colFechaVencimiento.setPrefWidth(100);
        
        TableColumn<Pago, String> colMetodoPago = new TableColumn<>("Método");
        colMetodoPago.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        colMetodoPago.setPrefWidth(100);
        
        TableColumn<Pago, String> colReferencia = new TableColumn<>("Referencia");
        colReferencia.setCellValueFactory(new PropertyValueFactory<>("numeroReferencia"));
        colReferencia.setPrefWidth(120);
        
        tablaPagos.getColumns().addAll(colFecha, colConcepto, colTipo, colMonto, colMontoPagado, colSaldoPendiente, colEstado, 
                colFechaVencimiento, colMetodoPago, colReferencia);
        
        // Agregar efecto hover
        tablaPagos.setRowFactory(tv -> {
            TableRow<Pago> row = new TableRow<>();
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #ecf0f1;");
                }
            });
            row.setOnMouseExited(event -> {
                row.setStyle("");
            });
            return row;
        });
        
        container.getChildren().addAll(tituloTabla, tablaPagos);
        VBox.setVgrow(tablaPagos, Priority.ALWAYS);
        
        return container;
    }

    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 1px 0 0 0;");
        
        // Información para el usuario
        Label infoLabel = new Label("💡 Seleccione un pago para generar recibo o estado de cuenta");
        infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 10px;");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnGenerarRecibo = new Button("🧾 Generar Recibo");
        btnGenerarRecibo.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        btnGenerarRecibo.setOnAction(e -> generarRecibo());
        
        Button btnEstadoCuenta = new Button("📊 Estado de Cuenta");
        btnEstadoCuenta.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        btnEstadoCuenta.setOnAction(e -> generarEstadoCuenta());
        
        Button btnCerrar = new Button("❌ Cerrar");
        btnCerrar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        btnCerrar.setOnAction(e -> stage.close());
        
        footer.getChildren().addAll(infoLabel, spacer, btnGenerarRecibo, btnEstadoCuenta, btnCerrar);
        return footer;
    }

    private void cargarDatosAlumno() {
        try {
            // Buscar el alumno asociado al usuario actual
            List<Alumno> todosAlumnos = AlumnoController.obtenerAlumnos();
            alumnoActual = todosAlumnos.stream()
                    .filter(alumno -> alumno.getEmail() != null && 
                            alumno.getEmail().equals(usuarioActual.getEmail()))
                    .findFirst()
                    .orElse(null);
            
            if (alumnoActual == null) {
                mostrarError("No se pudo encontrar el registro de alumno asociado a este usuario.");
                stage.close();
            }
        } catch (Exception e) {
            mostrarError("Error al cargar datos del alumno: " + e.getMessage());
        }
    }

    private void cargarPagos() {
        if (alumnoActual == null) return;
        
        try {
            // Aplicar filtros (esto cargará los pagos filtrados)
            filtrarPagos();
            
        } catch (Exception e) {
            mostrarError("Error al cargar pagos: " + e.getMessage());
        }
    }

    private void filtrarPagos() {
        if (alumnoActual == null) return;
        
        try {
            // Obtener todos los pagos del alumno
            List<Pago> todosPagos = PagoController.obtenerPagosPorAlumno(alumnoActual.getId());
            
            // Aplicar filtros
            List<Pago> pagosFiltrados = todosPagos.stream()
                .filter(pago -> {
                    // Filtro por tipo
                    TipoPago tipoFiltro = filtroTipoCombo.getValue();
                    if (tipoFiltro != null && !tipoFiltro.equals(pago.getTipo())) {
                        return false;
                    }
                    
                    // Filtro por estado
                    EstadoPago estadoFiltro = filtroEstadoCombo.getValue();
                    if (estadoFiltro != null && !estadoFiltro.equals(pago.getEstado())) {
                        return false;
                    }
                    
                    // Filtro por fecha
                    LocalDate fechaPago = pago.getFechaPago();
                    if (fechaPago != null) {
                        LocalDate desde = fechaDesde.getValue();
                        LocalDate hasta = fechaHasta.getValue();
                        
                        if (desde != null && fechaPago.isBefore(desde)) {
                            return false;
                        }
                        if (hasta != null && fechaPago.isAfter(hasta)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
            
            // Actualizar la tabla con los datos filtrados
            listaPagos.clear();
            listaPagos.addAll(pagosFiltrados);
            
            // Actualizar estadísticas con los datos filtrados
            actualizarEstadisticas();
            
        } catch (Exception e) {
            mostrarError("Error al filtrar pagos: " + e.getMessage());
        }
    }

    private void limpiarFiltros() {
        // Limpiar los valores de los filtros
        filtroTipoCombo.setValue(null);
        filtroEstadoCombo.setValue(null);
        fechaDesde.setValue(LocalDate.now().minusMonths(3));
        fechaHasta.setValue(LocalDate.now());
        
        // Recargar pagos sin filtros
        filtrarPagos();
    }

    private void actualizarEstadisticas() {
        if (listaPagos.isEmpty()) {
            totalPagadoLabel.setText("$0.00");
            totalPendienteLabel.setText("$0.00");
            totalPagosLabel.setText("0");
            estadoCuentaLabel.setText("Sin datos");
            return;
        }

        // Calcular estadísticas
        double totalPagado = listaPagos.stream()
                .filter(p -> p.getEstado() != null && 
                           (p.getEstado().toString().toLowerCase().contains("pagado") ||
                            p.getEstado().toString().toLowerCase().contains("completado")))
                .mapToDouble(Pago::getMontoPagado)
                .sum();
        
        double totalPendiente = listaPagos.stream()
                .filter(p -> p.getEstado() != null && 
                           (p.getEstado().toString().toLowerCase().contains("pendiente") ||
                            p.getEstado().toString().toLowerCase().contains("vencido")))
                .mapToDouble(Pago::getMontoTotal)
                .sum();
        
        int totalPagos = listaPagos.size();
        
        // Actualizar labels
        totalPagadoLabel.setText(String.format("$%.2f", totalPagado));
        totalPendienteLabel.setText(String.format("$%.2f", totalPendiente));
        totalPagosLabel.setText(String.valueOf(totalPagos));
        
        // Determinar estado de cuenta
        String estadoCuenta;
        Color colorEstado;
        if (totalPendiente == 0) {
            estadoCuenta = "Al día";
            colorEstado = Color.web("#27ae60");
        } else if (totalPendiente <= totalPagado * 0.1) { // Menos del 10%
            estadoCuenta = "Aceptable";
            colorEstado = Color.web("#f39c12");
        } else {
            estadoCuenta = "Pendiente";
            colorEstado = Color.web("#e74c3c");
        }
        
        estadoCuentaLabel.setText(estadoCuenta);
        estadoCuentaLabel.setTextFill(colorEstado);
    }

    private void generarRecibo() {
        Pago pagoSeleccionado = tablaPagos.getSelectionModel().getSelectedItem();
        if (pagoSeleccionado == null) {
            mostrarError("Por favor seleccione un pago para generar el recibo.");
            return;
        }
        
        // Solo permitir generar recibos de pagos completados
        if (pagoSeleccionado.getEstado() != EstadoPago.PAGADO && 
            pagoSeleccionado.getEstado() != EstadoPago.BECADO && 
            pagoSeleccionado.getEstado() != EstadoPago.CONDONADO) {
            mostrarError("Solo se pueden generar recibos de pagos completados, becados o condonados.\\nEstado actual: " + pagoSeleccionado.getEstado().getNombre());
            return;
        }
        
        try {
            // Crear directorio de recibos si no existe
            File reciboDir = new File("recibos");
            if (!reciboDir.exists()) {
                reciboDir.mkdirs();
            }
            
            // Generar nombre de archivo único
            String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String nombreArchivo = String.format("Recibo_%s_%s_%s.pdf", 
                alumnoActual.getMatricula(),
                pagoSeleccionado.getFolio(),
                fechaActual);
            
            File archivoRecibo = new File(reciboDir, nombreArchivo);
            
            // Generar el PDF del recibo
            if (generarReciboPDF(pagoSeleccionado, archivoRecibo)) {
                // Mostrar diálogo de confirmación
                Alert confirmacion = new Alert(Alert.AlertType.INFORMATION);
                confirmacion.setTitle("Recibo Generado");
                confirmacion.setHeaderText("✅ Recibo generado exitosamente");
                confirmacion.setContentText("Archivo: " + nombreArchivo + "\\n\\n¿Desea abrir el archivo?");
                
                ButtonType btnAbrir = new ButtonType("Abrir");
                ButtonType btnCerrar = new ButtonType("Cerrar");
                confirmacion.getButtonTypes().setAll(btnAbrir, btnCerrar);
                
                confirmacion.showAndWait().ifPresent(response -> {
                    if (response == btnAbrir) {
                        // Ejecutar la apertura del archivo en un hilo separado para evitar bloqueo de la UI
                        javafx.concurrent.Task<Void> abrirArchivoTask = new javafx.concurrent.Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                try {
                                    if (Desktop.isDesktopSupported()) {
                                        Desktop.getDesktop().open(archivoRecibo);
                                    } else {
                                        javafx.application.Platform.runLater(() -> 
                                            mostrarError("No se puede abrir el archivo automáticamente. \\nUbicación: " + archivoRecibo.getAbsolutePath())
                                        );
                                    }
                                } catch (IOException e) {
                                    javafx.application.Platform.runLater(() -> 
                                        mostrarError("Error al abrir el archivo: " + e.getMessage())
                                    );
                                }
                                return null;
                            }
                        };
                        
                        Thread thread = new Thread(abrirArchivoTask);
                        thread.setDaemon(true);
                        thread.start();
                    }
                });
            } else {
                mostrarError("Error al generar el recibo. Intente nuevamente.");
            }
            
        } catch (Exception e) {
            mostrarError("Error inesperado al generar recibo: " + e.getMessage());
        }
    }

    private void generarEstadoCuenta() {
        try {
            // Crear directorio de estados de cuenta si no existe
            File estadoDir = new File("estados_cuenta");
            if (!estadoDir.exists()) {
                estadoDir.mkdirs();
            }
            
            // Generar nombre de archivo único
            String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String nombreArchivo = String.format("EstadoCuenta_%s_%s.pdf", 
                alumnoActual.getMatricula(),
                fechaActual);
            
            File archivoEstado = new File(estadoDir, nombreArchivo);
            
            // Obtener todos los pagos del alumno (sin filtros)
            List<Pago> todosPagos = PagoController.obtenerPagosPorAlumno(alumnoActual.getId());
            
            // Mostrar indicador de progreso
            Alert progreso = new Alert(Alert.AlertType.INFORMATION);
            progreso.setTitle("Generando Estado de Cuenta");
            progreso.setHeaderText("Por favor espere...");
            progreso.setContentText("Generando el estado de cuenta en PDF");
            progreso.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
            progreso.show();
            
            // Generar el PDF en un hilo separado
            javafx.concurrent.Task<Boolean> generarTask = new javafx.concurrent.Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return generarEstadoCuentaPDF(todosPagos, archivoEstado);
                }
            };
            
            generarTask.setOnSucceeded(e -> {
                progreso.close();
                if (generarTask.getValue()) {
                // Mostrar diálogo de confirmación
                Alert confirmacion = new Alert(Alert.AlertType.INFORMATION);
                confirmacion.setTitle("Estado de Cuenta Generado");
                confirmacion.setHeaderText("✅ Estado de cuenta generado exitosamente");
                confirmacion.setContentText("Archivo: " + nombreArchivo + "\\nUbicación: " + archivoEstado.getAbsolutePath() + "\\n\\n¿Desea abrir el archivo?");
                
                ButtonType btnAbrir = new ButtonType("Abrir Archivo");
                ButtonType btnMostrarCarpeta = new ButtonType("Mostrar Carpeta");
                ButtonType btnCerrar = new ButtonType("Cerrar");
                confirmacion.getButtonTypes().setAll(btnAbrir, btnMostrarCarpeta, btnCerrar);
                
                confirmacion.showAndWait().ifPresent(response -> {
                    if (response == btnAbrir) {
                        // Ejecutar la apertura del archivo en un hilo separado para evitar bloqueo de la UI
                        javafx.concurrent.Task<Void> abrirArchivoTask = new javafx.concurrent.Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                try {
                                    if (Desktop.isDesktopSupported()) {
                                        Desktop.getDesktop().open(archivoEstado);
                                    } else {
                                        javafx.application.Platform.runLater(() -> 
                                            mostrarError("No se puede abrir el archivo automáticamente. \\nUbicación: " + archivoEstado.getAbsolutePath())
                                        );
                                    }
                                } catch (IOException e) {
                                    javafx.application.Platform.runLater(() -> 
                                        mostrarError("Error al abrir el archivo: " + e.getMessage())
                                    );
                                }
                                return null;
                            }
                        };
                        
                        Thread thread = new Thread(abrirArchivoTask);
                        thread.setDaemon(true);
                        thread.start();
                    } else if (response == btnMostrarCarpeta) {
                        // Abrir la carpeta contenedora
                        javafx.concurrent.Task<Void> abrirCarpetaTask = new javafx.concurrent.Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                try {
                                    if (Desktop.isDesktopSupported()) {
                                        Desktop.getDesktop().open(archivoEstado.getParentFile());
                                    } else {
                                        javafx.application.Platform.runLater(() -> 
                                            mostrarError("No se puede abrir la carpeta automáticamente. \\nUbicación: " + archivoEstado.getParent())
                                        );
                                    }
                                } catch (IOException e) {
                                    javafx.application.Platform.runLater(() -> 
                                        mostrarError("Error al abrir la carpeta: " + e.getMessage())
                                    );
                                }
                                return null;
                            }
                        };
                        
                        Thread carpetaThread = new Thread(abrirCarpetaTask);
                        carpetaThread.setDaemon(true);
                        carpetaThread.start();
                    }
                });
                } else {
                    mostrarError("Error al generar el estado de cuenta. Intente nuevamente.");
                }
            });
            
            generarTask.setOnFailed(e -> {
                progreso.close();
                Throwable exception = generarTask.getException();
                mostrarError("Error inesperado al generar estado de cuenta: " + 
                           (exception != null ? exception.getMessage() : "Error desconocido"));
            });
            
            Thread thread = new Thread(generarTask);
            thread.setDaemon(true);
            thread.start();
            
        } catch (Exception e) {
            mostrarError("Error inesperado al generar estado de cuenta: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Para testing independiente
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    private boolean generarReciboPDF(Pago pago, File archivo) {
        try {
            // Verificaciones adicionales antes de generar
            if (pago == null) {
                System.err.println("Error: Pago es null");
                return false;
            }
            
            if (alumnoActual == null) {
                System.err.println("Error: Alumno actual es null");
                return false;
            }
            
            System.out.println("Generando recibo para pago: " + pago.getFolio());
            System.out.println("Alumno: " + alumnoActual.getNombre());
            System.out.println("Archivo de salida: " + archivo.getAbsolutePath());
            
            boolean resultado = PDFGenerator.generatePaymentReceipt(pago, alumnoActual, archivo.getAbsolutePath());
            
            if (resultado) {
                System.out.println("Recibo generado exitosamente");
            } else {
                System.err.println("PDFGenerator.generatePaymentReceipt devolvió false");
            }
            
            return resultado;
        } catch (Exception e) {
            System.err.println("Error al generar recibo PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean generarEstadoCuentaPDF(List<Pago> pagos, File archivo) {
        try {
            // Verificaciones adicionales antes de generar
            if (alumnoActual == null) {
                System.err.println("Error: Alumno actual es null");
                return false;
            }
            
            if (pagos == null) {
                System.err.println("Warning: Lista de pagos es null, generando reporte vacío");
                pagos = new java.util.ArrayList<>();
            }
            
            System.out.println("Generando estado de cuenta para: " + alumnoActual.getNombre());
            System.out.println("Número de pagos: " + pagos.size());
            System.out.println("Archivo de salida: " + archivo.getAbsolutePath());
            
            boolean resultado = PDFGenerator.generatePaymentReport(alumnoActual, pagos, archivo.getAbsolutePath());
            
            if (resultado) {
                System.out.println("Estado de cuenta generado exitosamente");
            } else {
                System.err.println("PDFGenerator.generatePaymentReport devolvió false");
            }
            
            return resultado;
        } catch (Exception e) {
            System.err.println("Error al generar estado de cuenta PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}