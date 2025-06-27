package com.controlescolar.views;

import com.controlescolar.controllers.AuthController;
import com.controlescolar.controllers.PadreAlumnoController;
import com.controlescolar.controllers.CalificacionController;
import com.controlescolar.controllers.AsistenciaController;
import com.controlescolar.controllers.PagoController;
import com.controlescolar.models.Alumno;
import com.controlescolar.models.Calificacion;
import com.controlescolar.models.Asistencia;
import com.controlescolar.models.Pago;
import com.controlescolar.models.Usuario;

import javafx.application.Application;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class PadresDashboardView extends Application {
    private Usuario usuarioActual;
    private Stage primaryStage;
    private ComboBox<Alumno> alumnosComboBox;
    private TabPane tabPane;
    private TableView<CalificacionData> calificacionesTable;
    private TableView<AsistenciaData> asistenciaTable;
    private TableView<PagoData> pagosTable;

    public PadresDashboardView(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        primaryStage.setTitle("Portal de Padres - Control Escolar");
        primaryStage.setMaximized(true);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));

        // Header
        HBox header = createHeader();
        
        // Selector de alumno
        HBox selectorPanel = createAlumnoSelector();
        
        // Tabs con información
        tabPane = createTabPane();
        
        mainLayout.getChildren().addAll(header, selectorPanel, tabPane);
        
        Scene scene = new Scene(mainLayout);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        cargarAlumnos();
    }

    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label titleLabel = new Label("Portal de Padres");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.DARKBLUE);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("Bienvenido: " + usuarioActual.getNombreCompleto());
        userLabel.setFont(Font.font("Arial", 14));

        Button logoutBtn = new Button("Cerrar Sesión");
        logoutBtn.setOnAction(e -> {
            AuthController.logout();
            primaryStage.close();
            // Aquí deberías abrir la ventana de login
        });

        header.getChildren().addAll(titleLabel, spacer, userLabel, logoutBtn);
        return header;
    }

    private HBox createAlumnoSelector() {
        HBox selectorPanel = new HBox(10);
        selectorPanel.setAlignment(Pos.CENTER_LEFT);
        selectorPanel.setPadding(new Insets(10));

        Label label = new Label("Seleccionar hijo:");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        alumnosComboBox = new ComboBox<>();
        alumnosComboBox.setOnAction(e -> actualizarDatos());
        
        Button generarPagoBtn = new Button("Generar Pago");
        generarPagoBtn.setOnAction(e -> mostrarDialogoGenerarPago());

        selectorPanel.getChildren().addAll(label, alumnosComboBox, generarPagoBtn);
        return selectorPanel;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        
        // Tab de calificaciones
        Tab calificacionesTab = new Tab("Calificaciones");
        calificacionesTab.setClosable(false);
        calificacionesTable = createCalificacionesTable();
        calificacionesTab.setContent(new ScrollPane(calificacionesTable));
        
        // Tab de asistencia
        Tab asistenciaTab = new Tab("Asistencia");
        asistenciaTab.setClosable(false);
        asistenciaTable = createAsistenciaTable();
        asistenciaTab.setContent(new ScrollPane(asistenciaTable));
        
        // Tab de pagos
        Tab pagosTab = new Tab("Pagos");
        pagosTab.setClosable(false);
        pagosTable = createPagosTable();
        pagosTab.setContent(new ScrollPane(pagosTable));
        
        tabPane.getTabs().addAll(calificacionesTab, asistenciaTab, pagosTab);
        return tabPane;
    }

    private TableView<CalificacionData> createCalificacionesTable() {
        TableView<CalificacionData> table = new TableView<>();
        
        TableColumn<CalificacionData, String> materiaCol = new TableColumn<>("Materia");
        materiaCol.setCellValueFactory(new PropertyValueFactory<>("materia"));
        
        TableColumn<CalificacionData, String> parcialCol = new TableColumn<>("Parcial");
        parcialCol.setCellValueFactory(new PropertyValueFactory<>("parcial"));
        
        TableColumn<CalificacionData, Double> calificacionCol = new TableColumn<>("Calificación");
        calificacionCol.setCellValueFactory(new PropertyValueFactory<>("calificacion"));
        
        TableColumn<CalificacionData, String> fechaCol = new TableColumn<>("Fecha");
        fechaCol.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        
        table.getColumns().addAll(materiaCol, parcialCol, calificacionCol, fechaCol);
        return table;
    }

    private TableView<AsistenciaData> createAsistenciaTable() {
        TableView<AsistenciaData> table = new TableView<>();
        
        TableColumn<AsistenciaData, String> fechaCol = new TableColumn<>("Fecha");
        fechaCol.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        
        TableColumn<AsistenciaData, String> materiaCol = new TableColumn<>("Materia");
        materiaCol.setCellValueFactory(new PropertyValueFactory<>("materia"));
        
        TableColumn<AsistenciaData, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        TableColumn<AsistenciaData, String> observacionesCol = new TableColumn<>("Observaciones");
        observacionesCol.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
        
        table.getColumns().addAll(fechaCol, materiaCol, estadoCol, observacionesCol);
        return table;
    }

    private TableView<PagoData> createPagosTable() {
        TableView<PagoData> table = new TableView<>();
        
        TableColumn<PagoData, String> conceptoCol = new TableColumn<>("Concepto");
        conceptoCol.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        
        TableColumn<PagoData, String> montoCol = new TableColumn<>("Monto");
        montoCol.setCellValueFactory(new PropertyValueFactory<>("monto"));
        
        TableColumn<PagoData, String> vencimientoCol = new TableColumn<>("Vencimiento");
        vencimientoCol.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        
        TableColumn<PagoData, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        TableColumn<PagoData, String> fechaPagoCol = new TableColumn<>("Fecha Pago");
        fechaPagoCol.setCellValueFactory(new PropertyValueFactory<>("fechaPago"));
        
        table.getColumns().addAll(conceptoCol, montoCol, vencimientoCol, estadoCol, fechaPagoCol);
        return table;
    }

    private void cargarAlumnos() {
        try {
            List<Alumno> alumnos = PadreAlumnoController.obtenerAlumnosPorPadre(usuarioActual.getId());
            
            ObservableList<Alumno> alumnosObservable = FXCollections.observableArrayList(alumnos);
            alumnosComboBox.setItems(alumnosObservable);
            
            if (!alumnos.isEmpty()) {
                alumnosComboBox.getSelectionModel().selectFirst();
                actualizarDatos();
            }
        } catch (Exception e) {
            mostrarError("Error al cargar alumnos: " + e.getMessage());
        }
    }

    private void actualizarDatos() {
        Alumno alumnoSeleccionado = alumnosComboBox.getSelectionModel().getSelectedItem();
        if (alumnoSeleccionado == null) return;

        cargarCalificaciones(alumnoSeleccionado.getId());
        cargarAsistencia(alumnoSeleccionado.getId());
        cargarPagos(alumnoSeleccionado.getId());
    }

    private void cargarCalificaciones(org.bson.types.ObjectId alumnoId) {
        try {
            List<Calificacion> calificaciones = CalificacionController.obtenerCalificacionesPorAlumno(alumnoId);
            ObservableList<CalificacionData> data = FXCollections.observableArrayList();
            
            for (Calificacion cal : calificaciones) {
                data.add(new CalificacionData(
                    cal.getMateria() != null ? cal.getMateria().getNombre() : "N/A",
                    cal.getPeriodo(),
                    cal.getCalificacion(),
                    cal.getFecha().toString()
                ));
            }
            
            calificacionesTable.setItems(data);
        } catch (Exception e) {
            mostrarError("Error al cargar calificaciones: " + e.getMessage());
        }
    }

    private void cargarAsistencia(org.bson.types.ObjectId alumnoId) {
        try {
            List<Asistencia> asistencias = AsistenciaController.obtenerAsistenciasPorAlumno(alumnoId);
            ObservableList<AsistenciaData> data = FXCollections.observableArrayList();
            
            for (Asistencia ast : asistencias) {
                data.add(new AsistenciaData(
                    ast.getFecha().toString(),
                    ast.getMateria() != null ? ast.getMateria().getNombre() : "N/A",
                    ast.getEstado().getDescripcion(),
                    ast.getObservaciones()
                ));
            }
            
            asistenciaTable.setItems(data);
        } catch (Exception e) {
            mostrarError("Error al cargar asistencia: " + e.getMessage());
        }
    }

    private void cargarPagos(org.bson.types.ObjectId alumnoId) {
        try {
            List<Pago> pagos = PagoController.obtenerPagosPorAlumno(alumnoId);
            ObservableList<PagoData> data = FXCollections.observableArrayList();
            
            for (Pago pago : pagos) {
                data.add(new PagoData(
                    pago.getConcepto(),
                    String.format("$%.2f", pago.getMontoOriginal()),
                    pago.getFechaVencimiento() != null ? pago.getFechaVencimiento().toString() : "",
                    pago.getEstado().getNombre(),
                    pago.getFechaPago() != null ? pago.getFechaPago().toString() : ""
                ));
            }
            
            pagosTable.setItems(data);
        } catch (Exception e) {
            mostrarError("Error al cargar pagos: " + e.getMessage());
        }
    }

    private void mostrarDialogoGenerarPago() {
        Alumno alumnoSeleccionado = alumnosComboBox.getSelectionModel().getSelectedItem();
        if (alumnoSeleccionado == null) {
            mostrarError("Seleccione un alumno primero.");
            return;
        }
        
        // Crear diálogo de pago
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Generar Pago");
        dialog.setHeaderText("Generar nuevo pago para: " + alumnoSeleccionado.getNombreCompleto());
        
        // Campos del formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField conceptoField = new TextField();
        TextField montoField = new TextField();
        TextArea observacionesField = new TextArea();
        observacionesField.setPrefRowCount(3);
        
        grid.add(new Label("Concepto:"), 0, 0);
        grid.add(conceptoField, 1, 0);
        grid.add(new Label("Monto:"), 0, 1);
        grid.add(montoField, 1, 1);
        grid.add(new Label("Observaciones:"), 0, 2);
        grid.add(observacionesField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType generarButtonType = new ButtonType("Generar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generarButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == generarButtonType) {
                try {
                    String concepto = conceptoField.getText().trim();
                    String montoText = montoField.getText().trim();
                    String observaciones = observacionesField.getText().trim();
                    
                    if (concepto.isEmpty() || montoText.isEmpty()) {
                        mostrarError("Complete los campos obligatorios.");
                        return false;
                    }
                    
                    double monto = Double.parseDouble(montoText);
                    
                    boolean exito = PagoController.generarPago(
                        alumnoSeleccionado.getId(),
                        concepto,
                        monto,
                        observaciones
                    );
                    
                    if (exito) {
                        mostrarInfo("Pago generado exitosamente.");
                        return true;
                    } else {
                        mostrarError("Error al generar el pago.");
                        return false;
                    }
                    
                } catch (NumberFormatException e) {
                    mostrarError("El monto debe ser un número válido.");
                    return false;
                } catch (Exception e) {
                    mostrarError("Error al generar pago: " + e.getMessage());
                    return false;
                }
            }
            return false;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                cargarPagos(alumnoSeleccionado.getId());
            }
        });
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Clases para datos de las tablas
    public static class CalificacionData {
        private String materia;
        private String parcial;
        private double calificacion;
        private String fecha;

        public CalificacionData(String materia, String parcial, double calificacion, String fecha) {
            this.materia = materia;
            this.parcial = parcial;
            this.calificacion = calificacion;
            this.fecha = fecha;
        }

        public String getMateria() { return materia; }
        public String getParcial() { return parcial; }
        public double getCalificacion() { return calificacion; }
        public String getFecha() { return fecha; }
    }

    public static class AsistenciaData {
        private String fecha;
        private String materia;
        private String estado;
        private String observaciones;

        public AsistenciaData(String fecha, String materia, String estado, String observaciones) {
            this.fecha = fecha;
            this.materia = materia;
            this.estado = estado;
            this.observaciones = observaciones;
        }

        public String getFecha() { return fecha; }
        public String getMateria() { return materia; }
        public String getEstado() { return estado; }
        public String getObservaciones() { return observaciones; }
    }

    public static class PagoData {
        private String concepto;
        private String monto;
        private String fechaVencimiento;
        private String estado;
        private String fechaPago;

        public PagoData(String concepto, String monto, String fechaVencimiento, String estado, String fechaPago) {
            this.concepto = concepto;
            this.monto = monto;
            this.fechaVencimiento = fechaVencimiento;
            this.estado = estado;
            this.fechaPago = fechaPago;
        }

        public String getConcepto() { return concepto; }
        public String getMonto() { return monto; }
        public String getFechaVencimiento() { return fechaVencimiento; }
        public String getEstado() { return estado; }
        public String getFechaPago() { return fechaPago; }
    }
}