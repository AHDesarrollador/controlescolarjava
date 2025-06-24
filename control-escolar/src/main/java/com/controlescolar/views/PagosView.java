//PagosView.Java
package com.controlescolar.views;

import com.controlescolar.controllers.PagoController;
import com.controlescolar.models.Pago;
import com.controlescolar.models.Alumno;
import com.controlescolar.enums.EstadoPago;
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
        // Ventana emergente para nuevo pago (simplificado)
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label lblInfo = new Label("Funcionalidad de 'Nuevo Pago' pendiente de implementación");
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setOnAction(e -> dialog.close());

        layout.getChildren().addAll(lblInfo, btnCerrar);
        dialog.setScene(new Scene(layout, 300, 150));
        dialog.setTitle("Nuevo Pago");
        dialog.show();
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
        if (seleccionado != null) {
            // Aquí podrías abrir un diálogo editable con campos ya llenados
            mostrarAlerta("Funcionalidad de 'Editar Pago' pendiente de implementación");
        } else {
            mostrarAlerta("Selecciona un pago para editar.");
        }
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
        }).toList();

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
}