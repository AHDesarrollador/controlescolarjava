//PagosView.Java
package com.controlescolar.views;

import com.controlescolar.controllers.PagoController;
import com.controlescolar.controllers.AlumnoController;
import com.controlescolar.models.Pago;
import com.controlescolar.models.Alumno;
import com.controlescolar.enums.EstadoPago;
import com.controlescolar.enums.TipoPago;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class PagosView {
    private Stage stage;
    private PagoController pagoController;
    private TableView<Pago> tablaPagos;
    private ObservableList<Pago> listaPagos;
    private ComboBox<String> comboEstado;
    private ComboBox<String> comboMes;
    private ComboBox<String> comboAnio;
    private TextField txtBuscarAlumno;

    public PagosView(Stage stage) {
        this.stage = stage;
        this.pagoController = new PagoController();
        this.listaPagos = FXCollections.observableArrayList();
        initializeView();
    }

    private void initializeView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Panel superior con filtros
        VBox topPanel = createTopPanel();

        // Tabla de pagos
        VBox centerPanel = createCenterPanel();

        // Panel de botones
        HBox bottomPanel = createBottomPanel();

        root.setTop(topPanel);
        root.setCenter(centerPanel);
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        stage.setTitle("Gestión de Pagos - Colegiaturas");
        stage.setScene(scene);

        cargarPagos();
    }

    private VBox createTopPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(0, 0, 20, 0));

        Label titulo = new Label("Gestión de Pagos - Colegiaturas");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Panel de filtros
        GridPane filtrosPanel = new GridPane();
        filtrosPanel.setHgap(15);
        filtrosPanel.setVgap(10);
        filtrosPanel.setAlignment(Pos.CENTER_LEFT);

        // Búsqueda por alumno
        Label lblBuscar = new Label("Buscar Alumno:");
        txtBuscarAlumno = new TextField();
        txtBuscarAlumno.setPromptText("Nombre o matrícula...");
        txtBuscarAlumno.setPrefWidth(200);
        txtBuscarAlumno.textProperty().addListener((obs, oldVal, newVal) -> filtrarPagos());

        // Estado del pago
        Label lblEstado = new Label("Estado:");
        comboEstado = new ComboBox<>();
        comboEstado.getItems().addAll("Todos", "PENDIENTE", "PAGADO", "VENCIDO");
        comboEstado.setValue("Todos");
        comboEstado.setPrefWidth(120);
        comboEstado.setOnAction(e -> filtrarPagos());

        // Mes
        Label lblMes = new Label("Mes:");
        comboMes = new ComboBox<>();
        comboMes.getItems().addAll("Todos", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");
        comboMes.setValue("Todos");
        comboMes.setPrefWidth(120);
        comboMes.setOnAction(e -> filtrarPagos());

        // Año
        Label lblAnio = new Label("Año:");
        comboAnio = new ComboBox<>();
        comboAnio.getItems().addAll("Todos", "2024", "2025", "2026");
        comboAnio.setValue("2025");
        comboAnio.setPrefWidth(100);
        comboAnio.setOnAction(e -> filtrarPagos());

        Button btnLimpiar = new Button("Limpiar Filtros");
        btnLimpiar.setOnAction(e -> limpiarFiltros());
        btnLimpiar.setStyle("-fx-background-color: #757575; -fx-text-fill: white;");

        filtrosPanel.add(lblBuscar, 0, 0);
        filtrosPanel.add(txtBuscarAlumno, 1, 0);
        filtrosPanel.add(lblEstado, 2, 0);
        filtrosPanel.add(comboEstado, 3, 0);
        filtrosPanel.add(lblMes, 0, 1);
        filtrosPanel.add(comboMes, 1, 1);
        filtrosPanel.add(lblAnio, 2, 1);
        filtrosPanel.add(comboAnio, 3, 1);
        filtrosPanel.add(btnLimpiar, 4, 1);

        panel.getChildren().addAll(titulo, filtrosPanel);
        return panel;
    }

    private VBox createCenterPanel() {
        VBox panel = new VBox(10);

        // Crear tabla
        tablaPagos = new TableView<>();
        tablaPagos.setItems(listaPagos);
        
        // Estilos para mejorar la visibilidad del texto
        tablaPagos.setStyle(
            "-fx-text-fill: black; " +
            "-fx-background-color: white; " +
            "-fx-control-inner-background: white; " +
            "-fx-control-inner-background-alt: #f4f4f4; " +
            "-fx-table-cell-border-color: #ddd; " +
            "-fx-table-header-border-color: #ddd;"
        );
        
        tablaPagos.setRowFactory(tv -> {
            TableRow<Pago> row = new TableRow<>();
            row.itemProperty().addListener((obs, previousPago, currentPago) -> {
                if (currentPago == null) {
                    row.setStyle("");
                } else {
                    switch (currentPago.getEstado()) {
                        case PAGADO:
                            row.setStyle("-fx-background-color: #E8F5E8;");
                            break;
                        case VENCIDO:
                            row.setStyle("-fx-background-color: #FFE8E8;");
                            break;
                        case PENDIENTE:
                            row.setStyle("-fx-background-color: #FFF8E1;");
                            break;
                        default:
                            row.setStyle("");
                    }
                }
            });
            return row;
        });

        // Columnas de la tabla
        TableColumn<Pago, String> colAlumno = new TableColumn<>("Alumno");
        colAlumno.setCellValueFactory(new PropertyValueFactory<>("nombreAlumno"));
        colAlumno.setPrefWidth(200);

        TableColumn<Pago, String> colMatricula = new TableColumn<>("Matrícula");
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matriculaAlumno"));
        colMatricula.setPrefWidth(120);

        TableColumn<Pago, String> colConcepto = new TableColumn<>("Concepto");
        colConcepto.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        colConcepto.setPrefWidth(150);

        TableColumn<Pago, Double> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colMonto.setCellFactory(column -> new TableCell<Pago, Double>() {
            @Override
            protected void updateItem(Double monto, boolean empty) {
                super.updateItem(monto, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", monto));
                }
            }
        });
        colMonto.setPrefWidth(100);

        TableColumn<Pago, String> colFechaVencimiento = new TableColumn<>("Vencimiento");
        colFechaVencimiento.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaVencimiento();
            return new javafx.beans.property.SimpleStringProperty(
                    fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""
            );
        });
        colFechaVencimiento.setPrefWidth(120);

        TableColumn<Pago, String> colFechaPago = new TableColumn<>("Fecha Pago");
        colFechaPago.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaPago();
            return new javafx.beans.property.SimpleStringProperty(
                    fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""
            );
        });
        colFechaPago.setPrefWidth(120);

        TableColumn<Pago, EstadoPago> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(column -> new TableCell<Pago, EstadoPago>() {
            @Override
            protected void updateItem(EstadoPago estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado.toString());
                    switch (estado) {
                        case PAGADO:
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case VENCIDO:
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        case PENDIENTE:
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
        colEstado.setPrefWidth(100);

        TableColumn<Pago, String> colObservaciones = new TableColumn<>("Observaciones");
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
        colObservaciones.setPrefWidth(200);

        tablaPagos.getColumns().addAll(colAlumno, colMatricula, colConcepto, colMonto,
                colFechaVencimiento, colFechaPago, colEstado, colObservaciones);

        // Resumen financiero
        HBox resumen = createResumenFinanciero();

        panel.getChildren().addAll(resumen, tablaPagos);
        return panel;
    }

    private HBox createResumenFinanciero() {
        HBox panel = new HBox(30);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Label lblTotalPagos = new Label("Total Registros: 0");
        Label lblPagados = new Label("Pagados: $0.00");
        Label lblPendientes = new Label("Pendientes: $0.00");
        Label lblVencidos = new Label("Vencidos: $0.00");
        Label lblTotal = new Label("Total: $0.00");

        lblTotalPagos.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        lblPagados.setStyle("-fx-font-weight: bold; -fx-text-fill: green; -fx-font-size: 14px;");
        lblPendientes.setStyle("-fx-font-weight: bold; -fx-text-fill: orange; -fx-font-size: 14px;");
        lblVencidos.setStyle("-fx-font-weight: bold; -fx-text-fill: red; -fx-font-size: 14px;");
        lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: blue; -fx-font-size: 14px;");

        panel.getChildren().addAll(lblTotalPagos, lblPagados, lblPendientes, lblVencidos, lblTotal);
        return panel;
    }

    private HBox createBottomPanel() {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(20, 0, 0, 0));

        Button btnNuevoPago = new Button("Nuevo Pago");
        btnNuevoPago.setOnAction(e -> mostrarDialogoNuevoPago());
        btnNuevoPago.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px;");

        Button btnRegistrarPago = new Button("Registrar Pago");
        btnRegistrarPago.setOnAction(e -> registrarPago());
        btnRegistrarPago.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 10px 20px;");

        Button btnEditarPago = new Button("Editar Pago");
        btnEditarPago.setOnAction(e -> editarPago());
        btnEditarPago.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-padding: 10px 20px;");

        Button btnEliminarPago = new Button("Eliminar Pago");
        btnEliminarPago.setOnAction(e -> eliminarPago());
        btnEliminarPago.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 10px 20px;");

        panel.getChildren().addAll(btnNuevoPago, btnRegistrarPago, btnEditarPago, btnEliminarPago);
        return panel;
    }

    private void mostrarDialogoNuevoPago() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Crear Nuevo Pago");
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Header
        Label titulo = new Label("💰 Crear Nuevo Pago");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        VBox headerBox = new VBox(10);
        headerBox.getChildren().add(titulo);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        root.setTop(headerBox);
        
        // Formulario
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setAlignment(Pos.TOP_LEFT);
        
        // Selección de alumno
        Label lblAlumno = new Label("Alumno:");
        lblAlumno.setStyle("-fx-font-weight: bold;");
        ComboBox<Alumno> comboAlumno = new ComboBox<>();
        comboAlumno.setPrefWidth(300);
        comboAlumno.setConverter(new javafx.util.StringConverter<Alumno>() {
            @Override
            public String toString(Alumno alumno) {
                return alumno == null ? "Seleccionar alumno..." : 
                       alumno.getMatricula() + " - " + alumno.getNombre() + " " + alumno.getApellidos();
            }
            @Override
            public Alumno fromString(String string) {
                return null;
            }
        });
        
        // Cargar alumnos
        try {
            List<Alumno> alumnos = AlumnoController.obtenerAlumnos();
            comboAlumno.getItems().addAll(alumnos);
        } catch (Exception e) {
            mostrarAlerta("Error al cargar alumnos: " + e.getMessage());
        }
        
        // Tipo de pago
        Label lblTipo = new Label("Tipo de Pago:");
        lblTipo.setStyle("-fx-font-weight: bold;");
        ComboBox<TipoPago> comboTipo = new ComboBox<>();
        comboTipo.getItems().addAll(TipoPago.values());
        comboTipo.setValue(TipoPago.COLEGIATURA);
        comboTipo.setPrefWidth(200);
        comboTipo.setConverter(new javafx.util.StringConverter<TipoPago>() {
            @Override
            public String toString(TipoPago tipo) {
                return tipo == null ? "" : tipo.getNombre();
            }
            @Override
            public TipoPago fromString(String string) {
                return null;
            }
        });
        
        // Concepto
        Label lblConcepto = new Label("Concepto:");
        lblConcepto.setStyle("-fx-font-weight: bold;");
        TextField txtConcepto = new TextField();
        txtConcepto.setPromptText("Ej: Colegiatura Enero 2025");
        txtConcepto.setPrefWidth(300);
        
        // Auto-completar concepto basado en tipo de pago
        comboTipo.setOnAction(e -> {
            TipoPago tipo = comboTipo.getValue();
            if (tipo != null) {
                String mes = getMesActual();
                String anio = String.valueOf(LocalDate.now().getYear());
                switch (tipo) {
                    case COLEGIATURA:
                        txtConcepto.setText("Colegiatura " + mes + " " + anio);
                        break;
                    case INSCRIPCION:
                        txtConcepto.setText("Inscripción " + anio);
                        break;
                    case REINSCRIPCION:
                        txtConcepto.setText("Reinscripción " + anio);
                        break;
                    default:
                        txtConcepto.setText(tipo.getNombre() + " " + mes + " " + anio);
                        break;
                }
            }
        });
        
        // Monto original
        Label lblMonto = new Label("Monto Original:");
        lblMonto.setStyle("-fx-font-weight: bold;");
        TextField txtMonto = new TextField();
        txtMonto.setPromptText("0.00");
        txtMonto.setPrefWidth(150);
        
        // Fecha de vencimiento
        Label lblVencimiento = new Label("Fecha de Vencimiento:");
        lblVencimiento.setStyle("-fx-font-weight: bold;");
        DatePicker dateVencimiento = new DatePicker();
        dateVencimiento.setValue(LocalDate.now().plusDays(30)); // 30 días por defecto
        dateVencimiento.setPrefWidth(200);
        
        // Periodo
        Label lblPeriodo = new Label("Periodo:");
        lblPeriodo.setStyle("-fx-font-weight: bold;");
        TextField txtPeriodo = new TextField();
        txtPeriodo.setText(getMesActual() + " " + LocalDate.now().getYear());
        txtPeriodo.setPrefWidth(200);
        
        // Observaciones
        Label lblObservaciones = new Label("Observaciones:");
        lblObservaciones.setStyle("-fx-font-weight: bold;");
        TextArea txtObservaciones = new TextArea();
        txtObservaciones.setPromptText("Observaciones adicionales...");
        txtObservaciones.setPrefRowCount(3);
        txtObservaciones.setPrefWidth(300);
        
        // Estado (solo lectura)
        Label lblEstado = new Label("Estado Inicial:");
        lblEstado.setStyle("-fx-font-weight: bold;");
        Label lblEstadoValue = new Label("PENDIENTE");
        lblEstadoValue.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        
        // Agregar campos al formulario
        form.add(lblAlumno, 0, 0);
        form.add(comboAlumno, 1, 0, 2, 1);
        
        form.add(lblTipo, 0, 1);
        form.add(comboTipo, 1, 1);
        
        form.add(lblConcepto, 0, 2);
        form.add(txtConcepto, 1, 2, 2, 1);
        
        form.add(lblMonto, 0, 3);
        form.add(txtMonto, 1, 3);
        
        form.add(lblVencimiento, 0, 4);
        form.add(dateVencimiento, 1, 4);
        
        form.add(lblPeriodo, 0, 5);
        form.add(txtPeriodo, 1, 5);
        
        form.add(lblEstado, 0, 6);
        form.add(lblEstadoValue, 1, 6);
        
        form.add(lblObservaciones, 0, 7);
        form.add(txtObservaciones, 1, 7, 2, 1);
        
        root.setCenter(form);
        
        // Botones
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button btnCancelar = new Button("❌ Cancelar");
        btnCancelar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10px 20px;");
        btnCancelar.setOnAction(e -> dialog.close());
        
        Button btnGuardar = new Button("💾 Crear Pago");
        btnGuardar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10px 20px;");
        btnGuardar.setOnAction(e -> {
            try {
                // Validaciones
                if (comboAlumno.getValue() == null) {
                    mostrarAlerta("Debe seleccionar un alumno.");
                    return;
                }
                
                if (txtConcepto.getText().trim().isEmpty()) {
                    mostrarAlerta("Debe ingresar un concepto.");
                    return;
                }
                
                double monto;
                try {
                    monto = Double.parseDouble(txtMonto.getText());
                    if (monto <= 0) {
                        mostrarAlerta("El monto debe ser mayor a 0.");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    mostrarAlerta("El monto debe ser un número válido.");
                    return;
                }
                
                if (dateVencimiento.getValue() == null) {
                    mostrarAlerta("Debe seleccionar una fecha de vencimiento.");
                    return;
                }
                
                // Crear el pago
                Pago nuevoPago = new Pago();
                nuevoPago.setAlumnoId(comboAlumno.getValue().getId());
                nuevoPago.setTipo(comboTipo.getValue());
                nuevoPago.setConcepto(txtConcepto.getText().trim());
                nuevoPago.setMontoOriginal(monto);
                nuevoPago.setMontoPagado(0.0);
                nuevoPago.setMontoRecargo(0.0);
                nuevoPago.setMontoBeca(0.0);
                nuevoPago.setFechaVencimiento(dateVencimiento.getValue());
                nuevoPago.setPeriodo(txtPeriodo.getText().trim());
                nuevoPago.setEstado(EstadoPago.PENDIENTE);
                nuevoPago.setObservaciones(txtObservaciones.getText().trim());
                nuevoPago.setResponsableRegistro("Administrador");
                
                // Generar folio automático
                String folio = "PAG" + System.currentTimeMillis();
                nuevoPago.setFolio(folio);
                
                // Guardar en base de datos
                if (PagoController.registrarPago(nuevoPago)) {
                    mostrarAlerta("Pago creado exitosamente.\nFolio: " + folio);
                    cargarPagos(); // Recargar la tabla
                    dialog.close();
                } else {
                    mostrarAlerta("Error al crear el pago. Intente nuevamente.");
                }
                
            } catch (Exception ex) {
                mostrarAlerta("Error inesperado: " + ex.getMessage());
            }
        });
        
        buttonBox.getChildren().addAll(btnCancelar, btnGuardar);
        root.setBottom(buttonBox);
        
        Scene scene = new Scene(root, 600, 600);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private String getMesActual() {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                         "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[LocalDate.now().getMonthValue() - 1];
    }

    private void registrarPago() {
        Pago seleccionado = tablaPagos.getSelectionModel().getSelectedItem();
        if (seleccionado != null && seleccionado.getEstado() == EstadoPago.PENDIENTE) {
            seleccionado.setFechaPago(LocalDate.now());
            seleccionado.setEstado(EstadoPago.PAGADO);
            pagoController.actualizarPago(seleccionado);
            cargarPagos();
        } else {
            mostrarAlerta("Selecciona un pago pendiente para registrar el pago.");
        }
    }

    private void editarPago() {
        Pago seleccionado = tablaPagos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Selecciona un pago para editar.");
            return;
        }
        
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Editar Pago");
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Header
        Label titulo = new Label("✏️ Editar Pago");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        VBox headerBox = new VBox(10);
        headerBox.getChildren().add(titulo);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        root.setTop(headerBox);
        
        // Formulario
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setAlignment(Pos.TOP_LEFT);
        
        // Folio (solo lectura)
        Label lblFolio = new Label("Folio:");
        lblFolio.setStyle("-fx-font-weight: bold;");
        TextField txtFolio = new TextField(seleccionado.getFolio());
        txtFolio.setEditable(false);
        txtFolio.setStyle("-fx-background-color: #f0f0f0;");
        
        // Concepto
        Label lblConcepto = new Label("Concepto:");
        lblConcepto.setStyle("-fx-font-weight: bold;");
        TextField txtConcepto = new TextField(seleccionado.getConcepto());
        txtConcepto.setPrefWidth(300);
        
        // Monto original
        Label lblMontoOriginal = new Label("Monto Original:");
        lblMontoOriginal.setStyle("-fx-font-weight: bold;");
        TextField txtMontoOriginal = new TextField(String.valueOf(seleccionado.getMontoOriginal()));
        txtMontoOriginal.setPrefWidth(150);
        
        // Monto pagado
        Label lblMontoPagado = new Label("Monto Pagado:");
        lblMontoPagado.setStyle("-fx-font-weight: bold;");
        TextField txtMontoPagado = new TextField(String.valueOf(seleccionado.getMontoPagado()));
        txtMontoPagado.setPrefWidth(150);
        
        // Monto recargo
        Label lblMontoRecargo = new Label("Recargo:");
        lblMontoRecargo.setStyle("-fx-font-weight: bold;");
        TextField txtMontoRecargo = new TextField(String.valueOf(seleccionado.getMontoRecargo()));
        txtMontoRecargo.setPrefWidth(150);
        
        // Monto beca
        Label lblMontoBeca = new Label("Beca/Descuento:");
        lblMontoBeca.setStyle("-fx-font-weight: bold;");
        TextField txtMontoBeca = new TextField(String.valueOf(seleccionado.getMontoBeca()));
        txtMontoBeca.setPrefWidth(150);
        
        // Fecha de vencimiento
        Label lblVencimiento = new Label("Fecha de Vencimiento:");
        lblVencimiento.setStyle("-fx-font-weight: bold;");
        DatePicker dateVencimiento = new DatePicker(seleccionado.getFechaVencimiento());
        dateVencimiento.setPrefWidth(200);
        
        // Fecha de pago
        Label lblFechaPago = new Label("Fecha de Pago:");
        lblFechaPago.setStyle("-fx-font-weight: bold;");
        DatePicker datePago = new DatePicker(seleccionado.getFechaPago());
        datePago.setPrefWidth(200);
        
        // Estado
        Label lblEstado = new Label("Estado:");
        lblEstado.setStyle("-fx-font-weight: bold;");
        ComboBox<EstadoPago> comboEstado = new ComboBox<>();
        comboEstado.getItems().addAll(EstadoPago.values());
        comboEstado.setValue(seleccionado.getEstado());
        comboEstado.setPrefWidth(150);
        
        // Método de pago
        Label lblMetodoPago = new Label("Método de Pago:");
        lblMetodoPago.setStyle("-fx-font-weight: bold;");
        ComboBox<String> comboMetodo = new ComboBox<>();
        comboMetodo.getItems().addAll("Efectivo", "Transferencia", "Tarjeta de Crédito", "Tarjeta de Débito", "Cheque");
        comboMetodo.setValue(seleccionado.getMetodoPago());
        comboMetodo.setPrefWidth(200);
        
        // Observaciones
        Label lblObservaciones = new Label("Observaciones:");
        lblObservaciones.setStyle("-fx-font-weight: bold;");
        TextArea txtObservaciones = new TextArea(seleccionado.getObservaciones());
        txtObservaciones.setPrefRowCount(3);
        txtObservaciones.setPrefWidth(300);
        
        // Agregar campos al formulario
        form.add(lblFolio, 0, 0);
        form.add(txtFolio, 1, 0);
        
        form.add(lblConcepto, 0, 1);
        form.add(txtConcepto, 1, 1, 2, 1);
        
        form.add(lblMontoOriginal, 0, 2);
        form.add(txtMontoOriginal, 1, 2);
        
        form.add(lblMontoPagado, 0, 3);
        form.add(txtMontoPagado, 1, 3);
        
        form.add(lblMontoRecargo, 0, 4);
        form.add(txtMontoRecargo, 1, 4);
        
        form.add(lblMontoBeca, 0, 5);
        form.add(txtMontoBeca, 1, 5);
        
        form.add(lblVencimiento, 0, 6);
        form.add(dateVencimiento, 1, 6);
        
        form.add(lblFechaPago, 0, 7);
        form.add(datePago, 1, 7);
        
        form.add(lblEstado, 0, 8);
        form.add(comboEstado, 1, 8);
        
        form.add(lblMetodoPago, 0, 9);
        form.add(comboMetodo, 1, 9);
        
        form.add(lblObservaciones, 0, 10);
        form.add(txtObservaciones, 1, 10, 2, 1);
        
        root.setCenter(form);
        
        // Botones
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button btnCancelar = new Button("❌ Cancelar");
        btnCancelar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10px 20px;");
        btnCancelar.setOnAction(e -> dialog.close());
        
        Button btnGuardar = new Button("💾 Guardar Cambios");
        btnGuardar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10px 20px;");
        btnGuardar.setOnAction(e -> {
            try {
                // Validaciones
                if (txtConcepto.getText().trim().isEmpty()) {
                    mostrarAlerta("Debe ingresar un concepto.");
                    return;
                }
                
                double montoOriginal, montoPagado, montoRecargo, montoBeca;
                try {
                    montoOriginal = Double.parseDouble(txtMontoOriginal.getText());
                    montoPagado = Double.parseDouble(txtMontoPagado.getText());
                    montoRecargo = Double.parseDouble(txtMontoRecargo.getText());
                    montoBeca = Double.parseDouble(txtMontoBeca.getText());
                    
                    if (montoOriginal < 0 || montoPagado < 0 || montoRecargo < 0 || montoBeca < 0) {
                        mostrarAlerta("Los montos no pueden ser negativos.");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    mostrarAlerta("Todos los montos deben ser números válidos.");
                    return;
                }
                
                // Actualizar el pago
                seleccionado.setConcepto(txtConcepto.getText().trim());
                seleccionado.setMontoOriginal(montoOriginal);
                seleccionado.setMontoPagado(montoPagado);
                seleccionado.setMontoRecargo(montoRecargo);
                seleccionado.setMontoBeca(montoBeca);
                seleccionado.setFechaVencimiento(dateVencimiento.getValue());
                seleccionado.setFechaPago(datePago.getValue());
                seleccionado.setEstado(comboEstado.getValue());
                seleccionado.setMetodoPago(comboMetodo.getValue());
                seleccionado.setObservaciones(txtObservaciones.getText().trim());
                
                // Guardar en base de datos
                if (PagoController.actualizarPago(seleccionado)) {
                    mostrarAlerta("Pago actualizado exitosamente.");
                    cargarPagos(); // Recargar la tabla
                    dialog.close();
                } else {
                    mostrarAlerta("Error al actualizar el pago. Intente nuevamente.");
                }
                
            } catch (Exception ex) {
                mostrarAlerta("Error inesperado: " + ex.getMessage());
            }
        });
        
        buttonBox.getChildren().addAll(btnCancelar, btnGuardar);
        root.setBottom(buttonBox);
        
        Scene scene = new Scene(root, 650, 650);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void eliminarPago() {
        Pago seleccionado = tablaPagos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Eliminar Pago");
            confirmacion.setHeaderText("¿Estás seguro de eliminar este pago?");
            confirmacion.setContentText("Esta acción no se puede deshacer.");
            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                pagoController.eliminarPago(seleccionado.getId());
                cargarPagos();
            }
        } else {
            mostrarAlerta("Selecciona un pago para eliminar.");
        }
    }

    private void filtrarPagos() {
        String texto = txtBuscarAlumno.getText().toLowerCase().trim();
        String estadoFiltro = comboEstado.getValue();
        String mesFiltro = comboMes.getValue();
        String anioFiltro = comboAnio.getValue();

        List<Pago> pagosFiltrados = pagoController.obtenerTodos().stream().filter(p -> {
            boolean coincide = true;

            if (!texto.isEmpty()) {
                coincide &= p.getNombreAlumno().toLowerCase().contains(texto) ||
                        p.getMatriculaAlumno().toLowerCase().contains(texto);
            }

            if (!"Todos".equals(estadoFiltro)) {
                coincide &= p.getEstado().toString().equals(estadoFiltro);
            }

            if (!"Todos".equals(mesFiltro) && p.getFechaVencimiento() != null) {
                coincide &= p.getFechaVencimiento().getMonth().name().equalsIgnoreCase(mesFiltro.toUpperCase());
            }

            if (!"Todos".equals(anioFiltro) && p.getFechaVencimiento() != null) {
                coincide &= Integer.toString(p.getFechaVencimiento().getYear()).equals(anioFiltro);
            }

            return coincide;
        }).collect(java.util.stream.Collectors.toList());

        listaPagos.setAll(pagosFiltrados);
        actualizarResumenFinanciero(pagosFiltrados);
    }

    private void limpiarFiltros() {
        txtBuscarAlumno.clear();
        comboEstado.setValue("Todos");
        comboMes.setValue("Todos");
        comboAnio.setValue("2025");
        cargarPagos();
    }

    private void cargarPagos() {
        List<Pago> pagos = pagoController.obtenerTodos();
        listaPagos.setAll(pagos);
        actualizarResumenFinanciero(pagos);
    }

    private void actualizarResumenFinanciero(List<Pago> pagos) {
        double total = pagos.stream().mapToDouble(Pago::getMonto).sum();
        double pagados = pagos.stream().filter(p -> p.getEstado() == EstadoPago.PAGADO).mapToDouble(Pago::getMonto).sum();
        double pendientes = pagos.stream().filter(p -> p.getEstado() == EstadoPago.PENDIENTE).mapToDouble(Pago::getMonto).sum();
        double vencidos = pagos.stream().filter(p -> p.getEstado() == EstadoPago.VENCIDO).mapToDouble(Pago::getMonto).sum();

        // Asume que el resumen financiero tiene estos labels como campos si quieres actualizarlos dinámicamente.
        // Si no están accesibles como atributos, puedes guardar referencias a ellos como atributos de clase.
        // Aquí se puede mejorar si defines lblTotalPagos, lblPagados, etc. como atributos.
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    public void show() {
        stage.show();
    }
}