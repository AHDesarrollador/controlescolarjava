//AsistenciaView.java
package com.controlescolar.views;

import com.controlescolar.controllers.AsistenciaController;
import com.controlescolar.models.Alumno;
import com.controlescolar.models.Asistencia;
import com.controlescolar.models.Grupo;
import com.controlescolar.models.Materia;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AsistenciaView {
    private Stage stage;
    private AsistenciaController asistenciaController;
    private TableView<Asistencia> tablaAsistencia;
    private ObservableList<Asistencia> listaAsistencia;
    private ComboBox<Materia> comboMaterias;
    private ComboBox<Grupo> comboGrupos;
    private DatePicker fechaSelector;

    public AsistenciaView(Stage stage) {
        this.stage = stage;
        this.asistenciaController = new AsistenciaController();
        this.listaAsistencia = FXCollections.observableArrayList();
        initializeView();
    }

    private void initializeView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Panel superior con filtros
        VBox topPanel = createTopPanel();

        // Tabla de asistencia
        VBox centerPanel = createCenterPanel();

        // Panel de botones
        HBox bottomPanel = createBottomPanel();

        root.setTop(topPanel);
        root.setCenter(centerPanel);
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        stage.setTitle("Control de Asistencia");
        stage.setScene(scene);

        cargarDatosIniciales();
    }

    private VBox createTopPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(0, 0, 20, 0));

        Label titulo = new Label("Control de Asistencia");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Panel de filtros
        GridPane filtrosPanel = new GridPane();
        filtrosPanel.setHgap(15);
        filtrosPanel.setVgap(10);
        filtrosPanel.setAlignment(Pos.CENTER_LEFT);

        // Fecha
        Label lblFecha = new Label("Fecha:");
        fechaSelector = new DatePicker(LocalDate.now());
        fechaSelector.setPrefWidth(150);

        // Materia
        Label lblMateria = new Label("Materia:");
        comboMaterias = new ComboBox<>();
        comboMaterias.setPrefWidth(200);
        comboMaterias.setOnAction(e -> cargarGruposPorMateria());

        // Grupo
        Label lblGrupo = new Label("Grupo:");
        comboGrupos = new ComboBox<>();
        comboGrupos.setPrefWidth(150);
        comboGrupos.setOnAction(e -> cargarAsistencia());

        Button btnFiltrar = new Button("Filtrar");
        btnFiltrar.setOnAction(e -> cargarAsistencia());
        btnFiltrar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        filtrosPanel.add(lblFecha, 0, 0);
        filtrosPanel.add(fechaSelector, 1, 0);
        filtrosPanel.add(lblMateria, 2, 0);
        filtrosPanel.add(comboMaterias, 3, 0);
        filtrosPanel.add(lblGrupo, 0, 1);
        filtrosPanel.add(comboGrupos, 1, 1);
        filtrosPanel.add(btnFiltrar, 2, 1);

        panel.getChildren().addAll(titulo, filtrosPanel);
        return panel;
    }

    private VBox createCenterPanel() {
        VBox panel = new VBox(10);

        // Crear tabla
        tablaAsistencia = new TableView<>();
        tablaAsistencia.setItems(listaAsistencia);

        // Columnas de la tabla
        TableColumn<Asistencia, String> colAlumno = new TableColumn<>("Alumno");
        colAlumno.setCellValueFactory(new PropertyValueFactory<>("nombreAlumno"));
        colAlumno.setPrefWidth(200);

        TableColumn<Asistencia, String> colMatricula = new TableColumn<>("Matrícula");
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matriculaAlumno"));
        colMatricula.setPrefWidth(120);

        TableColumn<Asistencia, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );
        });
        colFecha.setPrefWidth(100);

        TableColumn<Asistencia, String> colMateria = new TableColumn<>("Materia");
        colMateria.setCellValueFactory(new PropertyValueFactory<>("nombreMateria"));
        colMateria.setPrefWidth(150);

        TableColumn<Asistencia, String> colGrupo = new TableColumn<>("Grupo");
        colGrupo.setCellValueFactory(new PropertyValueFactory<>("nombreGrupo"));
        colGrupo.setPrefWidth(100);

        TableColumn<Asistencia, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setPrefWidth(100);

        // Columna de acciones con CheckBox para marcar asistencia
        TableColumn<Asistencia, Boolean> colAsistio = new TableColumn<>("Asistió");
        colAsistio.setCellValueFactory(new PropertyValueFactory<>("presente"));
        colAsistio.setCellFactory(column -> new TableCell<Asistencia, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean presente, boolean empty) {
                super.updateItem(presente, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(presente != null && presente);
                    checkBox.setOnAction(e -> {
                        Asistencia asistencia = getTableView().getItems().get(getIndex());
                        asistencia.setPresente(checkBox.isSelected());
                        asistencia.setEstado(checkBox.isSelected() ? "Presente" : "Ausente");
                    });
                    setGraphic(checkBox);
                }
            }
        });
        colAsistio.setPrefWidth(80);

        tablaAsistencia.getColumns().addAll(colAlumno, colMatricula, colFecha,
                colMateria, colGrupo, colEstado, colAsistio);

        // Estadísticas
        HBox estadisticas = createEstadisticas();

        panel.getChildren().addAll(estadisticas, tablaAsistencia);
        return panel;
    }

    private HBox createEstadisticas() {
        HBox panel = new HBox(30);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5;");

        Label lblTotalAlumnos = new Label("Total Alumnos: 0");
        Label lblPresentes = new Label("Presentes: 0");
        Label lblAusentes = new Label("Ausentes: 0");
        Label lblPorcentaje = new Label("% Asistencia: 0%");

        lblTotalAlumnos.setStyle("-fx-font-weight: bold;");
        lblPresentes.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
        lblAusentes.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
        lblPorcentaje.setStyle("-fx-font-weight: bold; -fx-text-fill: blue;");

        panel.getChildren().addAll(lblTotalAlumnos, lblPresentes, lblAusentes, lblPorcentaje);
        return panel;
    }

    private HBox createBottomPanel() {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(20, 0, 0, 0));

        Button btnGenerarLista = new Button("Generar Lista");
        btnGenerarLista.setOnAction(e -> generarListaAsistencia());
        btnGenerarLista.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 10px 20px;");

        Button btnGuardarAsistencia = new Button("Guardar Asistencia");
        btnGuardarAsistencia.setOnAction(e -> guardarAsistencia());
        btnGuardarAsistencia.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px;");

        Button btnReporteAsistencia = new Button("Reporte de Asistencia");
        btnReporteAsistencia.setOnAction(e -> generarReporteAsistencia());
        btnReporteAsistencia.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 10px 20px;");

        Button btnVolver = new Button("Volver");
        btnVolver.setOnAction(e -> stage.close());
        btnVolver.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-padding: 10px 20px;");

        panel.getChildren().addAll(btnGenerarLista, btnGuardarAsistencia,
                btnReporteAsistencia, btnVolver);
        return panel;
    }

    private void cargarDatosIniciales() {
        // Cargar materias
        List<Materia> materias = asistenciaController.obtenerMaterias();
        comboMaterias.setItems(FXCollections.observableArrayList(materias));
    }

    private void cargarGruposPorMateria() {
        Materia materiaSeleccionada = comboMaterias.getValue();
        if (materiaSeleccionada != null) {
            List<Grupo> grupos = asistenciaController.obtenerGruposPorMateria(materiaSeleccionada.getId());
            comboGrupos.setItems(FXCollections.observableArrayList(grupos));
        }
    }

    private void cargarAsistencia() {
        Materia materia = comboMaterias.getValue();
        Grupo grupo = comboGrupos.getValue();
        LocalDate fecha = fechaSelector.getValue();

        if (materia != null && grupo != null && fecha != null) {
            List<Asistencia> asistencias = asistenciaController.obtenerAsistencia(
                    materia.getId(), grupo.getId(), fecha
            );
            listaAsistencia.setAll(asistencias);
            actualizarEstadisticas();
        }
    }

    private void generarListaAsistencia() {
        Materia materia = comboMaterias.getValue();
        Grupo grupo = comboGrupos.getValue();
        LocalDate fecha = fechaSelector.getValue();

        if (materia == null || grupo == null || fecha == null) {
            mostrarAlerta("Error", "Seleccione materia, grupo y fecha");
            return;
        }

        List<Alumno> alumnos = asistenciaController.obtenerAlumnosPorGrupo(grupo.getId());
        listaAsistencia.clear();

        for (Alumno alumno : alumnos) {
            Asistencia asistencia = new Asistencia();
            asistencia.setAlumnoId(alumno.getId());
            asistencia.setNombreAlumno(alumno.getNombre() + " " + alumno.getApellidos());
            asistencia.setMatriculaAlumno(alumno.getMatricula());
            asistencia.setMateriaId(materia.getId());
            asistencia.setNombreMateria(materia.getNombre());
            asistencia.setGrupoId(grupo.getId());
            asistencia.setNombreGrupo(grupo.getNombre());
            asistencia.setFecha(fecha);
            asistencia.setPresente(false);
            asistencia.setEstado("Ausente");

            listaAsistencia.add(asistencia);
        }

        actualizarEstadisticas();
    }

    private void guardarAsistencia() {
        if (listaAsistencia.isEmpty()) {
            mostrarAlerta("Error", "No hay datos de asistencia para guardar");
            return;
        }

        try {
            boolean exito = asistenciaController.guardarAsistencia(listaAsistencia);
            if (exito) {
                mostrarInfo("Éxito", "Asistencia guardada correctamente");
            } else {
                mostrarAlerta("Error", "No se pudo guardar la asistencia");
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al guardar: " + e.getMessage());
        }
    }

    private void generarReporteAsistencia() {
        Materia materia = comboMaterias.getValue();
        Grupo grupo = comboGrupos.getValue();

        if (materia == null || grupo == null) {
            mostrarAlerta("Error", "Seleccione materia y grupo");
            return;
        }

        try {
            boolean exito = asistenciaController.generarReporteAsistencia(
                    materia.getId(), grupo.getId(), fechaSelector.getValue()
            );
            if (exito) {
                mostrarInfo("Éxito", "Reporte generado correctamente");
            } else {
                mostrarAlerta("Error", "No se pudo generar el reporte");
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al generar reporte: " + e.getMessage());
        }
    }

    private void actualizarEstadisticas() {
        int total = listaAsistencia.size();
        int presentes = (int) listaAsistencia.stream().filter(Asistencia::isPresente).count();
        int ausentes = total - presentes;
        double porcentaje = total > 0 ? (presentes * 100.0 / total) : 0;

        // Actualizar labels de estadísticas (necesitarías mantener referencias a estos labels)
        // Por simplicidad, se omite la implementación completa aquí
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void show() {
        stage.show();
    }
}