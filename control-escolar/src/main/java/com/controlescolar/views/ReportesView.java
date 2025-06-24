//ReportesView.java
package com.controlescolar.views;

import com.controlescolar.controllers.CalificacionController;
import com.controlescolar.controllers.AsistenciaController;
import com.controlescolar.controllers.PagoController;
import com.controlescolar.controllers.AlumnoController;
import com.controlescolar.controllers.ProfesorController;
import com.controlescolar.controllers.MateriaController;
import com.controlescolar.models.*;
import com.controlescolar.utils.PDFGenerator;
import com.controlescolar.utils.ExcelExporter;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportesView extends Application {

    // Controllers
    private CalificacionController calificacionController;
    private AsistenciaController asistenciaController;
    private PagoController pagoController;
    private AlumnoController alumnoController;
    private ProfesorController profesorController;
    private MateriaController materiaController;

    // Utilidades
    private PDFGenerator pdfGenerator;
    private ExcelExporter excelExporter;

    // Componentes UI principales
    private TabPane mainTabPane;
    private Stage primaryStage;

    // Datos observables
    private ObservableList<Alumno> alumnos;
    private ObservableList<Profesor> profesores;
    private ObservableList<Materia> materias;

    public ReportesView() {
        initializeControllers();
        initializeData();
    }

    private void initializeControllers() {
        this.calificacionController = new CalificacionController();
        this.asistenciaController = new AsistenciaController();
        this.pagoController = new PagoController();
        this.alumnoController = new AlumnoController();
        this.profesorController = new ProfesorController();
        this.materiaController = new MateriaController();
        this.pdfGenerator = new PDFGenerator();
        this.excelExporter = new ExcelExporter();
    }

    private void initializeData() {
        this.alumnos = FXCollections.observableArrayList(alumnoController.obtenerTodos());
        this.profesores = FXCollections.observableArrayList(profesorController.obtenerTodos());
        this.materias = FXCollections.observableArrayList(materiaController.obtenerTodas());
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setTitle("Sistema Control Escolar - Reportes");

        VBox root = createMainLayout();
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createMainLayout() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("main-container");

        // Header
        Label headerLabel = new Label("Reportes y Estadísticas");
        headerLabel.getStyleClass().add("header-title");

        // TabPane principal
        mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Crear tabs
        Tab calificacionesTab = createCalificacionesTab();
        Tab asistenciaTab = createAsistenciaTab();
        Tab pagosTab = createPagosTab();
        Tab estadisticasTab = createEstadisticasTab();

        mainTabPane.getTabs().addAll(calificacionesTab, asistenciaTab, pagosTab, estadisticasTab);

        root.getChildren().addAll(headerLabel, mainTabPane);
        VBox.setVgrow(mainTabPane, Priority.ALWAYS);

        return root;
    }

    private Tab createCalificacionesTab() {
        Tab tab = new Tab("Reportes de Calificaciones");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Filtros
        HBox filtros = createCalificacionesFiltros();

        // Tabla de calificaciones
        TableView<Calificacion> tablaCalificaciones = createCalificacionesTable();

        // Botones de exportación
        HBox botonesExport = createExportButtons("calificaciones", tablaCalificaciones);

        content.getChildren().addAll(filtros, tablaCalificaciones, botonesExport);
        VBox.setVgrow(tablaCalificaciones, Priority.ALWAYS);

        tab.setContent(content);
        return tab;
    }

    private HBox createCalificacionesFiltros() {
        HBox filtros = new HBox(15);
        filtros.setAlignment(Pos.CENTER_LEFT);
        filtros.setPadding(new Insets(10));
        filtros.getStyleClass().add("filtros-container");

        // Filtro por materia
        Label materiaLabel = new Label("Materia:");
        ComboBox<Materia> materiaCombo = new ComboBox<>();
        materiaCombo.setItems(materias);
        materiaCombo.setConverter(new StringConverter<Materia>() {
            @Override
            public String toString(Materia materia) {
                return materia != null ? materia.getNombre() : "";
            }

            @Override
            public Materia fromString(String string) {
                return materias.stream()
                        .filter(m -> m.getNombre().equals(string))
                        .findFirst().orElse(null);
            }
        });

        // Filtro por grupo
        Label grupoLabel = new Label("Grupo:");
        TextField grupoField = new TextField();
        grupoField.setPromptText("Ej: 3A");

        // Filtro por período
        Label periodoLabel = new Label("Período:");
        DatePicker fechaInicio = new DatePicker();
        fechaInicio.setPromptText("Fecha inicio");
        Label aLabel = new Label("a");
        DatePicker fechaFin = new DatePicker();
        fechaFin.setPromptText("Fecha fin");

        Button filtrarBtn = new Button("Filtrar");
        filtrarBtn.getStyleClass().add("btn-primary");
        filtrarBtn.setOnAction(e -> aplicarFiltrosCalificaciones(materiaCombo, grupoField, fechaInicio, fechaFin));

        Button limpiarBtn = new Button("Limpiar");
        limpiarBtn.getStyleClass().add("btn-secondary");
        limpiarBtn.setOnAction(e -> limpiarFiltrosCalificaciones(materiaCombo, grupoField, fechaInicio, fechaFin));

        filtros.getChildren().addAll(
                materiaLabel, materiaCombo,
                grupoLabel, grupoField,
                periodoLabel, fechaInicio, aLabel, fechaFin,
                filtrarBtn, limpiarBtn
        );

        return filtros;
    }

    private TableView<Calificacion> createCalificacionesTable() {
        TableView<Calificacion> tabla = new TableView<>();

        // Columnas
        TableColumn<Calificacion, String> alumnoCol = new TableColumn<>("Alumno");
        alumnoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getAlumno().getNombre() + " " +
                                data.getValue().getAlumno().getApellidos()
                )
        );

        TableColumn<Calificacion, String> materiaCol = new TableColumn<>("Materia");
        materiaCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getMateria().getNombre())
        );

        TableColumn<Calificacion, String> grupoCol = new TableColumn<>("Grupo");
        grupoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getGrupo().getNombre())
        );

        TableColumn<Calificacion, Double> calificacionCol = new TableColumn<>("Calificación");
        calificacionCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleDoubleProperty(data.getValue().getCalificacion()).asObject()
        );

        TableColumn<Calificacion, String> tipoCol = new TableColumn<>("Tipo");
        tipoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTipo().toString())
        );

        TableColumn<Calificacion, String> fechaCol = new TableColumn<>("Fecha");
        fechaCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                )
        );

        tabla.getColumns().addAll(alumnoCol, materiaCol, grupoCol, calificacionCol, tipoCol, fechaCol);

        // Cargar datos iniciales
        cargarCalificaciones(tabla);

        return tabla;
    }

    private Tab createAsistenciaTab() {
        Tab tab = new Tab("Reportes de Asistencia");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Filtros específicos para asistencia
        HBox filtros = createAsistenciaFiltros();

        // Gráfica de asistencia
        VBox graficaContainer = new VBox(10);
        Label graficaTitle = new Label("Estadísticas de Asistencia");
        graficaTitle.getStyleClass().add("section-title");

        PieChart asistenciaChart = createAsistenciaChart();
        graficaContainer.getChildren().addAll(graficaTitle, asistenciaChart);

        // Tabla de asistencia detallada
        TableView<Asistencia> tablaAsistencia = createAsistenciaTable();

        // Botones de exportación
        HBox botonesExport = createExportButtons("asistencia", tablaAsistencia);

        content.getChildren().addAll(filtros, graficaContainer, tablaAsistencia, botonesExport);
        VBox.setVgrow(tablaAsistencia, Priority.ALWAYS);

        tab.setContent(content);
        return tab;
    }

    private HBox createAsistenciaFiltros() {
        HBox filtros = new HBox(15);
        filtros.setAlignment(Pos.CENTER_LEFT);
        filtros.setPadding(new Insets(10));
        filtros.getStyleClass().add("filtros-container");

        Label alumnoLabel = new Label("Alumno:");
        ComboBox<Alumno> alumnoCombo = new ComboBox<>();
        alumnoCombo.setItems(alumnos);
        alumnoCombo.setConverter(new StringConverter<Alumno>() {
            @Override
            public String toString(Alumno alumno) {
                return alumno != null ? alumno.getNombre() + " " + alumno.getApellidos() : "";
            }

            @Override
            public Alumno fromString(String string) {
                return alumnos.stream()
                        .filter(a -> (a.getNombre() + " " + a.getApellidos()).equals(string))
                        .findFirst().orElse(null);
            }
        });

        Label fechaLabel = new Label("Período:");
        DatePicker fechaInicio = new DatePicker();
        DatePicker fechaFin = new DatePicker();

        Button filtrarBtn = new Button("Filtrar");
        filtrarBtn.getStyleClass().add("btn-primary");

        Button limpiarBtn = new Button("Limpiar");
        limpiarBtn.getStyleClass().add("btn-secondary");

        filtros.getChildren().addAll(
                alumnoLabel, alumnoCombo,
                fechaLabel, fechaInicio, new Label("a"), fechaFin,
                filtrarBtn, limpiarBtn
        );

        return filtros;
    }

    private PieChart createAsistenciaChart() {
        // Obtener datos de asistencia
        List<Asistencia> asistencias = asistenciaController.obtenerTodas();

        long presentes = asistencias.stream().filter(Asistencia::isPresente).count();
        long ausentes = asistencias.size() - presentes;

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Presentes", presentes),
                new PieChart.Data("Ausentes", ausentes)
        );

        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Distribución de Asistencia");
        chart.setLegendSide(javafx.geometry.Side.RIGHT);

        return chart;
    }

    private TableView<Asistencia> createAsistenciaTable() {
        TableView<Asistencia> tabla = new TableView<>();

        TableColumn<Asistencia, String> alumnoCol = new TableColumn<>("Alumno");
        alumnoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getAlumno().getNombre() + " " +
                                data.getValue().getAlumno().getApellidos()
                )
        );

        TableColumn<Asistencia, String> materiaCol = new TableColumn<>("Materia");
        materiaCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getMateria().getNombre())
        );

        TableColumn<Asistencia, String> fechaCol = new TableColumn<>("Fecha");
        fechaCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                )
        );

        TableColumn<Asistencia, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().isPresente() ? "Presente" : "Ausente"
                )
        );

        tabla.getColumns().addAll(alumnoCol, materiaCol, fechaCol, estadoCol);

        // Cargar datos
        ObservableList<Asistencia> asistencias = FXCollections.observableArrayList(
                asistenciaController.obtenerTodas()
        );
        tabla.setItems(asistencias);

        return tabla;
    }

    private Tab createPagosTab() {
        Tab tab = new Tab("Reportes de Pagos");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Métricas de pagos
        HBox metricas = createPagosMetricas();

        // Gráfica de pagos por mes
        LineChart<String, Number> pagosChart = createPagosChart();

        // Tabla de pagos
        TableView<Pago> tablaPagos = createPagosTable();

        // Botones de exportación
        HBox botonesExport = createExportButtons("pagos", tablaPagos);

        content.getChildren().addAll(metricas, pagosChart, tablaPagos, botonesExport);
        VBox.setVgrow(tablaPagos, Priority.ALWAYS);

        tab.setContent(content);
        return tab;
    }

    private HBox createPagosMetricas() {
        HBox metricas = new HBox(20);
        metricas.setAlignment(Pos.CENTER);
        metricas.setPadding(new Insets(20));
        metricas.getStyleClass().add("metricas-container");

        List<Pago> pagos = pagoController.obtenerTodos();

        // Total recaudado
        double totalRecaudado = pagos.stream()
                .filter(p -> p.getEstado().toString().equals("PAGADO"))
                .mapToDouble(Pago::getMonto)
                .sum();

        VBox totalBox = createMetricaBox("Total Recaudado", String.format("$%.2f", totalRecaudado));

        // Pagos pendientes
        long pagosPendientes = pagos.stream()
                .filter(p -> p.getEstado().toString().equals("PENDIENTE"))
                .count();

        VBox pendientesBox = createMetricaBox("Pagos Pendientes", String.valueOf(pagosPendientes));

        // Porcentaje de cobranza
        double porcentajeCobranza = pagos.isEmpty() ? 0 :
                (double) pagos.stream().filter(p -> p.getEstado().toString().equals("PAGADO")).count() / pagos.size() * 100;

        VBox cobranzaBox = createMetricaBox("% Cobranza", String.format("%.1f%%", porcentajeCobranza));

        metricas.getChildren().addAll(totalBox, pendientesBox, cobranzaBox);

        return metricas;
    }

    private VBox createMetricaBox(String titulo, String valor) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("metrica-box");

        Label tituloLabel = new Label(titulo);
        tituloLabel.getStyleClass().add("metrica-titulo");

        Label valorLabel = new Label(valor);
        valorLabel.getStyleClass().add("metrica-valor");

        box.getChildren().addAll(tituloLabel, valorLabel);

        return box;
    }

    private LineChart<String, Number> createPagosChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Mes");
        yAxis.setLabel("Monto ($)");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Ingresos por Mes");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ingresos");

        // Aquí agregar datos reales agrupados por mes
        List<Pago> pagos = pagoController.obtenerTodos();
        Map<String, Double> pagosPorMes = pagos.stream()
                .filter(p -> p.getEstado().toString().equals("PAGADO"))
                .collect(Collectors.groupingBy(
                        p -> p.getFechaPago().format(DateTimeFormatter.ofPattern("MM/yyyy")),
                        Collectors.summingDouble(Pago::getMonto)
                ));

        pagosPorMes.forEach((mes, monto) -> series.getData().add(new XYChart.Data<>(mes, monto)));

        lineChart.getData().add(series);

        return lineChart;
    }

    private TableView<Pago> createPagosTable() {
        TableView<Pago> tabla = new TableView<>();

        TableColumn<Pago, String> alumnoCol = new TableColumn<>("Alumno");
        alumnoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getAlumno().getNombre() + " " +
                                data.getValue().getAlumno().getApellidos()
                )
        );

        TableColumn<Pago, Double> montoCol = new TableColumn<>("Monto");
        montoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleDoubleProperty(data.getValue().getMonto()).asObject()
        );

        TableColumn<Pago, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEstado().toString())
        );

        TableColumn<Pago, String> fechaCol = new TableColumn<>("Fecha Pago");
        fechaCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getFechaPago() != null ?
                                data.getValue().getFechaPago().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A"
                )
        );

        tabla.getColumns().addAll(alumnoCol, montoCol, estadoCol, fechaCol);

        ObservableList<Pago> pagos = FXCollections.observableArrayList(pagoController.obtenerTodos());
        tabla.setItems(pagos);

        return tabla;
    }

    private Tab createEstadisticasTab() {
        Tab tab = new Tab("Estadísticas Generales");

        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Dashboard de estadísticas generales
        GridPane dashboard = new GridPane();
        dashboard.setHgap(20);
        dashboard.setVgap(20);
        dashboard.setPadding(new Insets(20));

        // Estadísticas de alumnos
        VBox alumnosStats = createAlumnosStatsBox();

        // Estadísticas de profesores
        VBox profesoresStats = createProfesoresStatsBox();

        // Gráfica de rendimiento académico
        BarChart<String, Number> rendimientoChart = createRendimientoChart();

        dashboard.add(alumnosStats, 0, 0);
        dashboard.add(profesoresStats, 1, 0);
        dashboard.add(rendimientoChart, 0, 1, 2, 1);

        content.getChildren().add(dashboard);
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);

        tab.setContent(scrollPane);
        return tab;
    }

    private VBox createAlumnosStatsBox() {
        VBox box = new VBox(10);
        box.getStyleClass().add("stats-box");
        box.setPadding(new Insets(15));

        Label titulo = new Label("Estadísticas de Alumnos");
        titulo.getStyleClass().add("stats-title");

        Label totalAlumnos = new Label("Total: " + alumnos.size());
        Label alumnosActivos = new Label("Activos: " + alumnos.stream().filter(Alumno::isActivo).count());

        box.getChildren().addAll(titulo, totalAlumnos, alumnosActivos);

        return box;
    }

    private VBox createProfesoresStatsBox() {
        VBox box = new VBox(10);
        box.getStyleClass().add("stats-box");
        box.setPadding(new Insets(15));

        Label titulo = new Label("Estadísticas de Profesores");
        titulo.getStyleClass().add("stats-title");

        Label totalProfesores = new Label("Total: " + profesores.size());
        Label profesoresActivos = new Label("Activos: " + profesores.stream().filter(Profesor::isActivo).count());

        box.getChildren().addAll(titulo, totalProfesores, profesoresActivos);

        return box;
    }

    private BarChart<String, Number> createRendimientoChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Materias");
        yAxis.setLabel("Promedio");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Rendimiento Académico por Materia");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Promedio");

        // Calcular promedios por materia
        List<Calificacion> calificaciones = calificacionController.obtenerTodas();
        Map<String, Double> promediosPorMateria = calificaciones.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getMateria().getNombre(),
                        Collectors.averagingDouble(Calificacion::getCalificacion)
                ));

        promediosPorMateria.forEach((materia, promedio) ->
                series.getData().add(new XYChart.Data<>(materia, promedio))
        );

        barChart.getData().add(series);

        return barChart;
    }

    private HBox createExportButtons(String tipo, TableView<?> tabla) {
        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER_RIGHT);
        botones.setPadding(new Insets(10));

        Button exportPdfBtn = new Button("Exportar PDF");
        exportPdfBtn.getStyleClass().add("btn-export");
        exportPdfBtn.setOnAction(e -> exportarPDF(tipo, tabla));

        Button exportExcelBtn = new Button("Exportar Excel");
        exportExcelBtn.getStyleClass().add("btn-export");
        exportExcelBtn.setOnAction(e -> exportarExcel(tipo, tabla));

        botones.getChildren().addAll(exportPdfBtn, exportExcelBtn);

        return botones;
    }

    // Métodos de funcionalidad
    private void aplicarFiltrosCalificaciones(ComboBox<Materia> materiaCombo, TextField grupoField,
                                              DatePicker fechaInicio, DatePicker fechaFin) {
        // Implementar lógica de filtrado
        // Actualizar la tabla según los filtros seleccionados
    }

    private void limpiarFiltrosCalificaciones(ComboBox<Materia> materiaCombo, TextField grupoField,
                                              DatePicker fechaInicio, DatePicker fechaFin) {
        materiaCombo.setValue(null);
        grupoField.clear();
        fechaInicio.setValue(null);
        fechaFin.setValue(null);
    }

    private void cargarCalificaciones(TableView<Calificacion> tabla) {
        ObservableList<Calificacion> calificaciones = FXCollections.observableArrayList(
                calificacionController.obtenerTodas()
        );
        tabla.setItems(calificaciones);
    }

    private void exportarPDF(String tipo, TableView<?> tabla) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName("reporte_" + tipo + "_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                pdfGenerator.generarReporte(tipo, tabla.getItems(), file.getAbsolutePath());
                mostrarAlerta("Éxito", "Reporte PDF generado exitosamente", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al generar el PDF: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void exportarExcel(String tipo, TableView<?> tabla) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        fileChooser.setInitialFileName("reporte_" + tipo + "_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                excelExporter.exportarReporte(tipo, tabla.getItems(), file.getAbsolutePath());
                mostrarAlerta("Éxito", "Reporte Excel generado exitosamente", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al generar el Excel: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}