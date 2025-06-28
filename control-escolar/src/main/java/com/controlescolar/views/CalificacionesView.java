//CalificacionesView.java
package com.controlescolar.views;

import com.controlescolar.controllers.CalificacionController;
import com.controlescolar.controllers.AlumnoController;
import com.controlescolar.controllers.MateriaController;
import com.controlescolar.models.Calificacion;
import com.controlescolar.models.Alumno;
import com.controlescolar.models.Materia;
import com.controlescolar.models.Usuario;
import org.bson.types.ObjectId;
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
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CalificacionesView extends Application {

    // CalificacionController usa métodos estáticos
    // AlumnoController usa métodos estáticos
    private MateriaController materiaController;
    private Usuario usuarioActual;
    private Stage primaryStage;

    // Componentes UI
    private TableView<Calificacion> tablaCalificaciones;
    private ObservableList<Calificacion> listaCalificaciones;
    private TextField buscarField;
    private Button btnAgregar, btnEditar, btnEliminar, btnRefrescar;
    private ComboBox<Materia> filtroMateriaCombo;
    private ComboBox<String> filtroPeriodoCombo;

    // Formulario
    private ComboBox<Alumno> estudianteCombo;
    private ComboBox<Materia> materiaCombo;
    private TextField notaField;
    private ComboBox<String> tipoEvaluacionCombo, periodoCombo;
    private DatePicker fechaEvaluacionDatePicker;
    private TextArea observacionesArea;

    // Labels para estadísticas
    private Label totalLabel, promedioLabel, aprobadosLabel, reprobadosLabel;

    public CalificacionesView(Usuario usuario) {
        this.usuarioActual = usuario;
        // Los controladores usan métodos estáticos
        this.materiaController = new MateriaController();
        this.listaCalificaciones = FXCollections.observableArrayList();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setTitle("Gestión de Calificaciones");
        primaryStage.setWidth(1300);
        primaryStage.setHeight(700);

        // Layout principal
        BorderPane mainLayout = new BorderPane();

        // Header
        VBox header = createHeader();
        mainLayout.setTop(header);

        // Contenido principal
        VBox content = createMainContent();
        mainLayout.setCenter(content);

        Scene scene = new Scene(mainLayout);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        // Cargar datos iniciales
        cargarCalificaciones();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #2c3e50;");

        // Título
        Label titulo = new Label("Gestión de Calificaciones");
        titulo.setTextFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Barra de búsqueda y filtros
        HBox toolBar = createToolBar();

        header.getChildren().addAll(titulo, toolBar);
        return header;
    }

    private HBox createToolBar() {
        HBox toolBar = new HBox(10);
        toolBar.setAlignment(Pos.CENTER_LEFT);

        // Campo de búsqueda
        Label buscarLabel = new Label("Buscar:");
        buscarLabel.setTextFill(Color.WHITE);

        buscarField = new TextField();
        buscarField.setPromptText("Buscar por estudiante...");
        buscarField.setPrefWidth(200);
        buscarField.textProperty().addListener((obs, oldText, newText) -> filtrarCalificaciones());

        // Filtro por materia
        Label materiaLabel = new Label("Materia:");
        materiaLabel.setTextFill(Color.WHITE);

        filtroMateriaCombo = new ComboBox<>();
        filtroMateriaCombo.setPrefWidth(150);
        filtroMateriaCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrarCalificaciones());
        cargarMateriasParaFiltro();

        // Filtro por período
        Label periodoLabel = new Label("Período:");
        periodoLabel.setTextFill(Color.WHITE);

        filtroPeriodoCombo = new ComboBox<>();
        filtroPeriodoCombo.getItems().addAll("Todos", "Primer Parcial", "Segundo Parcial", "Tercer Parcial", "Final");
        filtroPeriodoCombo.setValue("Todos");
        filtroPeriodoCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrarCalificaciones());

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botones
        btnAgregar = new Button("➕ Agregar");
        btnAgregar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAgregar.setOnAction(e -> mostrarFormularioAgregar());

        btnEditar = new Button("✏️ Editar");
        btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnEditar.setOnAction(e -> mostrarFormularioEditar());
        btnEditar.setDisable(true);

        btnEliminar = new Button("🗑️ Eliminar");
        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnEliminar.setOnAction(e -> eliminarCalificacion());
        btnEliminar.setDisable(true);

        btnRefrescar = new Button("🔄 Refrescar");
        btnRefrescar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        btnRefrescar.setOnAction(e -> cargarCalificaciones());

        toolBar.getChildren().addAll(buscarLabel, buscarField, materiaLabel, filtroMateriaCombo,
                periodoLabel, filtroPeriodoCombo, spacer, btnAgregar, btnEditar, btnEliminar, btnRefrescar);
        return toolBar;
    }

    private VBox createMainContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Estadísticas rápidas
        HBox estadisticas = createEstadisticas();

        // Tabla de calificaciones
        tablaCalificaciones = createTablaCalificaciones();

        content.getChildren().addAll(estadisticas, tablaCalificaciones);
        return content;
    }

    private HBox createEstadisticas() {
        HBox estadisticas = new HBox(20);
        estadisticas.setPadding(new Insets(10));
        estadisticas.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5;");

        totalLabel = new Label("Total Calificaciones: 0");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        promedioLabel = new Label("Promedio General: 0.00");
        promedioLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        aprobadosLabel = new Label("Aprobados: 0");
        aprobadosLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        aprobadosLabel.setTextFill(Color.GREEN);

        reprobadosLabel = new Label("Reprobados: 0");
        reprobadosLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        reprobadosLabel.setTextFill(Color.RED);

        estadisticas.getChildren().addAll(totalLabel, promedioLabel, aprobadosLabel, reprobadosLabel);
        return estadisticas;
    }

    private TableView<Calificacion> createTablaCalificaciones() {
        TableView<Calificacion> tabla = new TableView<>();
        tabla.setItems(listaCalificaciones);
        
        // Estilos para mejorar la visibilidad del texto
        tabla.setStyle(
            "-fx-text-fill: black; " +
            "-fx-background-color: white; " +
            "-fx-control-inner-background: white; " +
            "-fx-control-inner-background-alt: #f4f4f4; " +
            "-fx-table-cell-border-color: #ddd; " +
            "-fx-table-header-border-color: #ddd;"
        );

        // Columnas
        TableColumn<Calificacion, String> colEstudianteMatricula = new TableColumn<>("Matrícula");
        colEstudianteMatricula.setCellValueFactory(cellData -> {
            Calificacion calificacion = cellData.getValue();
            // El modelo usa ObjectIds, no objetos directos
            String matricula = calificacion.getAlumnoId() != null ? 
                    calificacion.getAlumnoId().toString() : "";
            return new javafx.beans.property.SimpleStringProperty(matricula);
        });
        colEstudianteMatricula.setPrefWidth(100);

        TableColumn<Calificacion, String> colEstudianteNombre = new TableColumn<>("Estudiante");
        colEstudianteNombre.setCellValueFactory(cellData -> {
            Calificacion calificacion = cellData.getValue();
            String nombreCompleto = obtenerNombreEstudiante(calificacion.getAlumnoId());
            return new javafx.beans.property.SimpleStringProperty(nombreCompleto);
        });
        colEstudianteNombre.setPrefWidth(200);

        TableColumn<Calificacion, String> colMateria = new TableColumn<>("Materia");
        colMateria.setCellValueFactory(cellData -> {
            Calificacion calificacion = cellData.getValue();
            String nombreMateria = obtenerNombreMateria(calificacion.getMateriaId());
            return new javafx.beans.property.SimpleStringProperty(nombreMateria);
        });
        colMateria.setPrefWidth(150);

        TableColumn<Calificacion, Double> colNota = new TableColumn<>("Nota");
        colNota.setCellValueFactory(new PropertyValueFactory<>("calificacion"));
        colNota.setPrefWidth(80);

        // Colorear celda de nota según el valor
        colNota.setCellFactory(column -> new TableCell<Calificacion, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", item));
                    if (item >= 70) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                    } else if (item >= 60) {
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                    } else {
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                    }
                }
            }
        });

        TableColumn<Calificacion, String> colTipoEvaluacion = new TableColumn<>("Tipo");
        colTipoEvaluacion.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colTipoEvaluacion.setPrefWidth(120);

        TableColumn<Calificacion, String> colPeriodo = new TableColumn<>("Período");
        colPeriodo.setCellValueFactory(new PropertyValueFactory<>("periodo"));
        colPeriodo.setPrefWidth(120);

        TableColumn<Calificacion, LocalDate> colFechaEvaluacion = new TableColumn<>("Fecha");
        colFechaEvaluacion.setCellValueFactory(new PropertyValueFactory<>("fechaRegistro"));
        colFechaEvaluacion.setPrefWidth(100);

        TableColumn<Calificacion, String> colObservaciones = new TableColumn<>("Observaciones");
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
        colObservaciones.setPrefWidth(200);

        tabla.getColumns().addAll(colEstudianteMatricula, colEstudianteNombre, colMateria, colNota,
                colTipoEvaluacion, colPeriodo, colFechaEvaluacion, colObservaciones);

        // Listener para selección
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean seleccionValida = newSelection != null;
            btnEditar.setDisable(!seleccionValida);
            btnEliminar.setDisable(!seleccionValida);
        });

        // Doble click para editar
        tabla.setRowFactory(tv -> {
            TableRow<Calificacion> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    mostrarFormularioEditar();
                }
            });
            return row;
        });

        return tabla;
    }

    private void cargarMateriasParaFiltro() {
        try {
            List<Materia> materias = MateriaController.obtenerMaterias();
            filtroMateriaCombo.getItems().clear();
            filtroMateriaCombo.getItems().add(null); // Opción "Todas"
            filtroMateriaCombo.getItems().addAll(materias);

            filtroMateriaCombo.setConverter(new StringConverter<Materia>() {
                @Override
                public String toString(Materia materia) {
                    return materia == null ? "Todas las materias" : materia.getNombre();
                }

                @Override
                public Materia fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            mostrarError("Error al cargar materias: " + e.getMessage());
        }
    }

    private void cargarCalificaciones() {
        try {
            List<Calificacion> calificaciones = CalificacionController.obtenerTodas();
            listaCalificaciones.clear();
            listaCalificaciones.addAll(calificaciones);
            actualizarEstadisticas();
        } catch (Exception e) {
            mostrarError("Error al cargar calificaciones: " + e.getMessage());
        }
    }

    private void filtrarCalificaciones() {
        String textoBusqueda = buscarField.getText();
        Materia materiaFiltro = filtroMateriaCombo.getValue();
        String periodoFiltro = filtroPeriodoCombo.getValue();

        try {
            // Método buscarConFiltros no existe, usar lista actual por ahora
            List<Calificacion> calificacionesFiltradas = new java.util.ArrayList<>(listaCalificaciones);
            listaCalificaciones.clear();
            listaCalificaciones.addAll(calificacionesFiltradas);
            actualizarEstadisticas();
        } catch (Exception e) {
            mostrarError("Error al filtrar calificaciones: " + e.getMessage());
        }
    }

    private void actualizarEstadisticas() {
        int total = listaCalificaciones.size();
        int aprobados = 0;
        int reprobados = 0;
        double sumaNotas = 0.0;

        for (Calificacion calificacion : listaCalificaciones) {
            double nota = calificacion.getCalificacion();
            sumaNotas += nota;

            if (nota >= 60) {
                aprobados++;
            } else {
                reprobados++;
            }
        }

        double promedio = total > 0 ? sumaNotas / total : 0.0;

        totalLabel.setText("Total Calificaciones: " + total);
        promedioLabel.setText("Promedio General: " + String.format("%.2f", promedio));
        aprobadosLabel.setText("Aprobados: " + aprobados);
        reprobadosLabel.setText("Reprobados: " + reprobados);
    }

    private void mostrarFormularioAgregar() {
        mostrarFormulario("Agregar Calificación", null);
    }

    private void mostrarFormularioEditar() {
        Calificacion seleccionada = tablaCalificaciones.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            mostrarFormulario("Editar Calificación", seleccionada);
        }
    }

    private void mostrarFormulario(String titulo, Calificacion calificacion) {
        Dialog<Calificacion> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);

        // Configurar botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        // Crear formulario
        GridPane grid = createFormulario();

        // Si es edición, llenar campos
        if (calificacion != null) {
            llenarFormulario(calificacion);
        }

        dialog.getDialogPane().setContent(grid);

        // Validar campos antes de habilitar el botón guardar
        Button guardarButton = (Button) dialog.getDialogPane().lookupButton(guardarButtonType);
        guardarButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validarFormulario()) {
                event.consume();
            }
        });

        // Resultado del diálogo
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                return construirCalificacionDesdeFormulario(calificacion);
            }
            return null;
        });

        Optional<Calificacion> resultado = dialog.showAndWait();

        resultado.ifPresent(cal -> {
            try {
                if (calificacion == null) {
                    // Agregar nueva calificación
                    CalificacionController.registrarCalificacion(cal);
                    mostrarInformacion("Calificación agregada exitosamente");
                } else {
                    // Editar calificación existente
                    CalificacionController.actualizarCalificacion(cal);
                    mostrarInformacion("Calificación actualizada exitosamente");
                }
                cargarCalificaciones();
            } catch (Exception e) {
                mostrarError("Error al guardar calificación: " + e.getMessage());
            }
        });
    }

    private GridPane createFormulario() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Estudiante
        Label estudianteLabel = new Label("Estudiante:");
        estudianteCombo = new ComboBox<>();
        estudianteCombo.setPrefWidth(200);
        cargarEstudiantes();

        // Materia
        Label materiaLabel = new Label("Materia:");
        materiaCombo = new ComboBox<>();
        materiaCombo.setPrefWidth(200);
        cargarMaterias();

        // Nota
        Label notaLabel = new Label("Nota:");
        notaField = new TextField();
        notaField.setPromptText("0.00 - 100.00");

        // Tipo de evaluación
        Label tipoLabel = new Label("Tipo de Evaluación:");
        tipoEvaluacionCombo = new ComboBox<>();
        // Usar enum TipoCalificacion
        for (com.controlescolar.enums.TipoCalificacion tipo : com.controlescolar.enums.TipoCalificacion.values()) {
            tipoEvaluacionCombo.getItems().add(tipo.getNombre());
        }
        tipoEvaluacionCombo.setValue(com.controlescolar.enums.TipoCalificacion.EXAMEN_PARCIAL.getNombre());

        // Período
        Label periodoLabel = new Label("Período:");
        periodoCombo = new ComboBox<>();
        periodoCombo.getItems().addAll("Primer Parcial", "Segundo Parcial", "Tercer Parcial", "Final");
        periodoCombo.setValue("Primer Parcial");

        // Fecha de evaluación
        Label fechaLabel = new Label("Fecha de Evaluación:");
        fechaEvaluacionDatePicker = new DatePicker();
        fechaEvaluacionDatePicker.setValue(LocalDate.now());

        // Observaciones
        Label observacionesLabel = new Label("Observaciones:");
        observacionesArea = new TextArea();
        observacionesArea.setPrefRowCount(3);
        observacionesArea.setPromptText("Comentarios adicionales...");

        // Agregar campos al grid
        grid.add(estudianteLabel, 0, 0);
        grid.add(estudianteCombo, 1, 0);

        grid.add(materiaLabel, 0, 1);
        grid.add(materiaCombo, 1, 1);

        grid.add(notaLabel, 0, 2);
        grid.add(notaField, 1, 2);

        grid.add(tipoLabel, 0, 3);
        grid.add(tipoEvaluacionCombo, 1, 3);

        grid.add(periodoLabel, 0, 4);
        grid.add(periodoCombo, 1, 4);

        grid.add(fechaLabel, 0, 5);
        grid.add(fechaEvaluacionDatePicker, 1, 5);

        grid.add(observacionesLabel, 0, 6);
        grid.add(observacionesArea, 1, 6);

        return grid;
    }

    private void cargarEstudiantes() {
        try {
            List<Alumno> estudiantes = AlumnoController.obtenerAlumnos();
            estudianteCombo.getItems().clear();
            estudianteCombo.getItems().addAll(estudiantes);

            estudianteCombo.setConverter(new StringConverter<Alumno>() {
                @Override
                public String toString(Alumno alumno) {
                    return alumno == null ? "" :
                            alumno.getMatricula() + " - " + alumno.getNombre() + " " + alumno.getApellidos();
                }

                @Override
                public Alumno fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            mostrarError("Error al cargar estudiantes: " + e.getMessage());
        }
    }

    private void cargarMaterias() {
        try {
            List<Materia> materias = MateriaController.obtenerMaterias();
            materiaCombo.getItems().clear();
            materiaCombo.getItems().addAll(materias);

            materiaCombo.setConverter(new StringConverter<Materia>() {
                @Override
                public String toString(Materia materia) {
                    return materia == null ? "" : materia.getNombre();
                }

                @Override
                public Materia fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            mostrarError("Error al cargar materias: " + e.getMessage());
        }
    }

    private void llenarFormulario(Calificacion calificacion) {
        // El modelo usa ObjectIds, no objetos directos
        // estudianteCombo.setValue(calificacion.getAlumnoId());
        // materiaCombo.setValue(calificacion.getMateriaId());
        notaField.setText(String.valueOf(calificacion.getCalificacion()));
        tipoEvaluacionCombo.setValue(calificacion.getTipo().getNombre());
        periodoCombo.setValue(calificacion.getPeriodo());
        fechaEvaluacionDatePicker.setValue(calificacion.getFechaRegistro().toLocalDate());
        observacionesArea.setText(calificacion.getObservaciones());
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (estudianteCombo.getValue() == null) {
            errores.append("- Debe seleccionar un estudiante\n");
        }

        if (materiaCombo.getValue() == null) {
            errores.append("- Debe seleccionar una materia\n");
        }

        if (notaField.getText().trim().isEmpty()) {
            errores.append("- La nota es obligatoria\n");
        } else {
            try {
                double nota = Double.parseDouble(notaField.getText().trim());
                if (nota < 0 || nota > 100) {
                    errores.append("- La nota debe estar entre 0 y 100\n");
                }
            } catch (NumberFormatException e) {
                errores.append("- La nota debe ser un número válido\n");
            }
        }

        if (tipoEvaluacionCombo.getValue() == null || tipoEvaluacionCombo.getValue().trim().isEmpty()) {
            errores.append("- Debe seleccionar un tipo de evaluación\n");
        }

        if (periodoCombo.getValue() == null || periodoCombo.getValue().trim().isEmpty()) {
            errores.append("- Debe seleccionar un período\n");
        }

        if (fechaEvaluacionDatePicker.getValue() == null) {
            errores.append("- Debe seleccionar una fecha de evaluación\n");
        }

        if (errores.length() > 0) {
            mostrarError("Por favor corrija los siguientes errores:\n\n" + errores.toString());
            return false;
        }

        return true;
    }

    private Calificacion construirCalificacionDesdeFormulario(Calificacion calificacionExistente) {
        Calificacion calificacion;

        if (calificacionExistente != null) {
            calificacion = calificacionExistente;
        } else {
            calificacion = new Calificacion();
        }

        // Configurar ObjectIds en lugar de objetos directos
        if (estudianteCombo.getValue() != null) {
            calificacion.setAlumnoId(estudianteCombo.getValue().getId());
        }
        if (materiaCombo.getValue() != null) {
            calificacion.setMateriaId(materiaCombo.getValue().getId());
        }
        calificacion.setCalificacion(Double.parseDouble(notaField.getText().trim()));
        // Buscar el tipo por nombre
        com.controlescolar.enums.TipoCalificacion tipoSeleccionado = 
            com.controlescolar.enums.TipoCalificacion.fromNombre(tipoEvaluacionCombo.getValue());
        if (tipoSeleccionado != null) {
            calificacion.setTipo(tipoSeleccionado);
        }
        calificacion.setPeriodo(periodoCombo.getValue());
        calificacion.setFechaRegistro(fechaEvaluacionDatePicker.getValue().atStartOfDay());
        calificacion.setObservaciones(observacionesArea.getText());

        return calificacion;
    }

    private void eliminarCalificacion() {
        Calificacion seleccionada = tablaCalificaciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Debe seleccionar una calificación para eliminar");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar esta calificación?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                CalificacionController.eliminarCalificacion(seleccionada.getId());
                mostrarInformacion("Calificación eliminada exitosamente");
                cargarCalificaciones();
            } catch (Exception e) {
                mostrarError("Error al eliminar calificación: " + e.getMessage());
            }
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInformacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private String obtenerNombreEstudiante(ObjectId alumnoId) {
        if (alumnoId == null) return "Sin asignar";
        try {
            Alumno alumno = AlumnoController.obtenerAlumnoPorId(alumnoId);
            if (alumno != null) {
                return alumno.getNombre() + " " + alumno.getApellidos();
            }
        } catch (Exception e) {
            System.err.println("Error al obtener estudiante: " + e.getMessage());
        }
        return "ID: " + alumnoId.toString();
    }

    private String obtenerNombreMateria(ObjectId materiaId) {
        if (materiaId == null) return "Sin asignar";
        try {
            Materia materia = MateriaController.obtenerMateriaPorId(materiaId);
            if (materia != null) {
                return materia.getNombre();
            }
        } catch (Exception e) {
            System.err.println("Error al obtener materia: " + e.getMessage());
        }
        return "ID: " + materiaId.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}