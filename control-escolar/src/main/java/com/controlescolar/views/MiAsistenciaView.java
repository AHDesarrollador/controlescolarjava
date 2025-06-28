//MiAsistenciaView.java
package com.controlescolar.views;

import com.controlescolar.controllers.AsistenciaController;
import com.controlescolar.controllers.AlumnoController;
import com.controlescolar.controllers.MateriaController;
import com.controlescolar.models.Asistencia;
import com.controlescolar.models.Alumno;
import com.controlescolar.models.Materia;
import com.controlescolar.models.Usuario;
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

public class MiAsistenciaView extends Application {
    private Stage stage;
    private Usuario usuarioActual;
    private Alumno alumnoActual;
    private TableView<Asistencia> tablaAsistencias;
    private ObservableList<Asistencia> listaAsistencias;
    private ComboBox<Materia> filtroMateriaCombo;
    private ComboBox<String> filtroEstadoCombo;
    private DatePicker fechaDesde;
    private DatePicker fechaHasta;
    
    // Labels para estadísticas
    private Label totalClasesLabel;
    private Label presenciasLabel;
    private Label ausenciasLabel;
    private Label porcentajeAsistenciaLabel;

    public MiAsistenciaView(Usuario usuario) {
        this.usuarioActual = usuario;
        this.listaAsistencias = FXCollections.observableArrayList();
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Mi Asistencia - Sistema Control Escolar");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        root.setTop(createHeader());
        
        // Centro - contenido principal
        VBox centerContent = new VBox(15);
        centerContent.setPadding(new Insets(20));
        
        // Estadísticas de asistencia
        centerContent.getChildren().add(createEstadisticasPanel());
        
        // Filtros
        centerContent.getChildren().add(createFiltrosPanel());
        
        // Tabla de asistencias
        centerContent.getChildren().add(createTablaAsistencias());
        
        root.setCenter(centerContent);
        
        // Footer
        root.setBottom(createFooter());
        
        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.show();
        
        // Cargar datos
        cargarDatosAlumno();
        cargarAsistencias();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #27ae60;");
        
        Label titulo = new Label("📅 Mi Asistencia");
        titulo.setTextFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        Label subtitulo = new Label("Consulta tu historial de asistencias por materia");
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
        
        // Total de clases
        VBox totalBox = createStatCard("📚 Total Clases", "0", Color.web("#3498db"));
        totalClasesLabel = (Label) ((VBox) totalBox.getChildren().get(0)).getChildren().get(1);
        
        // Presencias
        VBox presenciasBox = createStatCard("✅ Presencias", "0", Color.web("#27ae60"));
        presenciasLabel = (Label) ((VBox) presenciasBox.getChildren().get(0)).getChildren().get(1);
        
        // Ausencias
        VBox ausenciasBox = createStatCard("❌ Ausencias", "0", Color.web("#e74c3c"));
        ausenciasLabel = (Label) ((VBox) ausenciasBox.getChildren().get(0)).getChildren().get(1);
        
        // Porcentaje de asistencia
        VBox porcentajeBox = createStatCard("📊 % Asistencia", "0%", Color.web("#9b59b6"));
        porcentajeAsistenciaLabel = (Label) ((VBox) porcentajeBox.getChildren().get(0)).getChildren().get(1);
        
        estadisticas.getChildren().addAll(totalBox, presenciasBox, ausenciasBox, porcentajeBox);
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
        
        // Filtro por materia
        Label materiaLabel = new Label("Materia:");
        materiaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        filtroMateriaCombo = new ComboBox<>();
        filtroMateriaCombo.setPrefWidth(200);
        filtroMateriaCombo.setConverter(new javafx.util.StringConverter<Materia>() {
            @Override
            public String toString(Materia materia) {
                return materia == null ? "Todas las materias" : materia.getNombre();
            }
            @Override
            public Materia fromString(String string) {
                return null;
            }
        });
        filtroMateriaCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrarAsistencias());
        
        // Filtro por estado
        Label estadoLabel = new Label("Estado:");
        estadoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        filtroEstadoCombo = new ComboBox<>();
        filtroEstadoCombo.getItems().addAll("Todos", "Presente", "Ausente", "Tardanza", "Justificado");
        filtroEstadoCombo.setValue("Todos");
        filtroEstadoCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrarAsistencias());
        
        // Filtro por fecha desde
        Label fechaDesdeLabel = new Label("Desde:");
        fechaDesdeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        fechaDesde = new DatePicker();
        fechaDesde.setValue(LocalDate.now().minusMonths(1)); // Último mes por defecto
        fechaDesde.valueProperty().addListener((obs, oldVal, newVal) -> filtrarAsistencias());
        
        // Filtro por fecha hasta
        Label fechaHastaLabel = new Label("Hasta:");
        fechaHastaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        fechaHasta = new DatePicker();
        fechaHasta.setValue(LocalDate.now());
        fechaHasta.valueProperty().addListener((obs, oldVal, newVal) -> filtrarAsistencias());
        
        // Botón actualizar
        Button btnActualizar = new Button("🔄 Actualizar");
        btnActualizar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnActualizar.setOnAction(e -> cargarAsistencias());
        
        // Botón limpiar filtros
        Button btnLimpiar = new Button("🗑️ Limpiar Filtros");
        btnLimpiar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        btnLimpiar.setOnAction(e -> limpiarFiltros());
        
        filtros.getChildren().addAll(materiaLabel, filtroMateriaCombo, estadoLabel, filtroEstadoCombo,
                fechaDesdeLabel, fechaDesde, fechaHastaLabel, fechaHasta, btnActualizar, btnLimpiar);
        return filtros;
    }

    private VBox createTablaAsistencias() {
        VBox container = new VBox(10);
        
        Label tituloTabla = new Label("📋 Historial de Asistencias");
        tituloTabla.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        tablaAsistencias = new TableView<>();
        tablaAsistencias.setItems(listaAsistencias);
        
        // Estilos para mejorar la visibilidad del texto
        tablaAsistencias.setStyle(
            "-fx-text-fill: black; " +
            "-fx-background-color: white; " +
            "-fx-control-inner-background: white; " +
            "-fx-control-inner-background-alt: #f4f4f4; " +
            "-fx-table-cell-border-color: #ddd; " +
            "-fx-table-header-border-color: #ddd;"
        );
        
        // Columnas
        TableColumn<Asistencia, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFecha();
            return new javafx.beans.property.SimpleStringProperty(
                fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""
            );
        });
        colFecha.setPrefWidth(100);
        
        TableColumn<Asistencia, String> colMateria = new TableColumn<>("Materia");
        colMateria.setCellValueFactory(cellData -> {
            Asistencia asistencia = cellData.getValue();
            String nombreMateria = obtenerNombreMateria(asistencia.getMateriaId());
            return new javafx.beans.property.SimpleStringProperty(nombreMateria);
        });
        colMateria.setPrefWidth(200);
        
        TableColumn<Asistencia, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> {
            Asistencia asistencia = cellData.getValue();
            String estado = asistencia.getEstadoString() != null ? asistencia.getEstadoString() : 
                           (asistencia.isPresente() ? "Presente" : "Ausente");
            return new javafx.beans.property.SimpleStringProperty(estado);
        });
        colEstado.setCellFactory(column -> new TableCell<Asistencia, String>() {
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
                        case "presente":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Verde
                            break;
                        case "ausente":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Rojo
                            break;
                        case "tardanza":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); // Naranja
                            break;
                        case "justificado":
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
        
        TableColumn<Asistencia, String> colHora = new TableColumn<>("Hora");
        colHora.setCellValueFactory(cellData -> {
            // Asumiendo que tienes un campo hora en Asistencia, sino puedes usar la fecha de registro
            return new javafx.beans.property.SimpleStringProperty("08:00"); // Placeholder
        });
        colHora.setPrefWidth(80);
        
        TableColumn<Asistencia, String> colObservaciones = new TableColumn<>("Observaciones");
        colObservaciones.setCellValueFactory(cellData -> {
            String obs = cellData.getValue().getObservaciones();
            return new javafx.beans.property.SimpleStringProperty(obs != null ? obs : "");
        });
        colObservaciones.setPrefWidth(250);
        
        TableColumn<Asistencia, String> colSemana = new TableColumn<>("Día de la Semana");
        colSemana.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFecha();
            if (fecha != null) {
                String diaSemana = fecha.getDayOfWeek().getDisplayName(
                    java.time.format.TextStyle.FULL, 
                    java.util.Locale.of("es", "ES")
                );
                return new javafx.beans.property.SimpleStringProperty(
                    diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colSemana.setPrefWidth(120);
        
        tablaAsistencias.getColumns().addAll(colFecha, colMateria, colEstado, colHora, colSemana, colObservaciones);
        
        // Agregar efecto hover
        tablaAsistencias.setRowFactory(tv -> {
            TableRow<Asistencia> row = new TableRow<>();
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
        
        container.getChildren().addAll(tituloTabla, tablaAsistencias);
        VBox.setVgrow(tablaAsistencias, Priority.ALWAYS);
        
        return container;
    }

    private HBox createFooter() {
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 1px 0 0 0;");
        
        Button btnExportar = new Button("📊 Exportar Reporte");
        btnExportar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        btnExportar.setOnAction(e -> exportarReporte());
        
        Button btnCerrar = new Button("❌ Cerrar");
        btnCerrar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        btnCerrar.setOnAction(e -> stage.close());
        
        footer.getChildren().addAll(btnExportar, btnCerrar);
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

    private void cargarAsistencias() {
        if (alumnoActual == null) return;
        
        try {
            // Cargar materias para el filtro primero
            cargarMateriasParaFiltro();
            
            // Aplicar filtros (esto cargará las asistencias filtradas)
            filtrarAsistencias();
            
        } catch (Exception e) {
            mostrarError("Error al cargar asistencias: " + e.getMessage());
        }
    }

    private void cargarMateriasParaFiltro() {
        try {
            List<Materia> materias = MateriaController.obtenerMaterias();
            filtroMateriaCombo.getItems().clear();
            filtroMateriaCombo.getItems().add(null); // "Todas"
            filtroMateriaCombo.getItems().addAll(materias);
        } catch (Exception e) {
            System.err.println("Error al cargar materias para filtro: " + e.getMessage());
        }
    }

    private void filtrarAsistencias() {
        if (alumnoActual == null) return;
        
        try {
            // Obtener todas las asistencias del alumno
            List<Asistencia> todasAsistencias = AsistenciaController.obtenerAsistenciasPorAlumno(alumnoActual.getId());
            
            // Aplicar filtros
            List<Asistencia> asistenciasFiltradas = todasAsistencias.stream()
                .filter(asistencia -> {
                    // Filtro por materia
                    Materia materiaFiltro = filtroMateriaCombo.getValue();
                    if (materiaFiltro != null && !materiaFiltro.getId().equals(asistencia.getMateriaId())) {
                        return false;
                    }
                    
                    // Filtro por estado
                    String estadoFiltro = filtroEstadoCombo.getValue();
                    if (estadoFiltro != null && !"Todos".equals(estadoFiltro)) {
                        String estadoAsistencia = asistencia.getEstadoString() != null ? 
                            asistencia.getEstadoString() : 
                            (asistencia.isPresente() ? "Presente" : "Ausente");
                        if (!estadoFiltro.equals(estadoAsistencia)) {
                            return false;
                        }
                    }
                    
                    // Filtro por fecha
                    LocalDate fechaAsistencia = asistencia.getFecha();
                    if (fechaAsistencia != null) {
                        LocalDate desde = fechaDesde.getValue();
                        LocalDate hasta = fechaHasta.getValue();
                        
                        if (desde != null && fechaAsistencia.isBefore(desde)) {
                            return false;
                        }
                        if (hasta != null && fechaAsistencia.isAfter(hasta)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
            
            // Actualizar la tabla con los datos filtrados
            listaAsistencias.clear();
            listaAsistencias.addAll(asistenciasFiltradas);
            
            // Actualizar estadísticas con los datos filtrados
            actualizarEstadisticas();
            
        } catch (Exception e) {
            mostrarError("Error al filtrar asistencias: " + e.getMessage());
        }
    }

    private void limpiarFiltros() {
        // Limpiar los valores de los filtros
        filtroMateriaCombo.setValue(null);
        filtroEstadoCombo.setValue("Todos");
        fechaDesde.setValue(LocalDate.now().minusMonths(1));
        fechaHasta.setValue(LocalDate.now());
        
        // Recargar asistencias sin filtros
        filtrarAsistencias();
    }

    private void actualizarEstadisticas() {
        if (listaAsistencias.isEmpty()) {
            totalClasesLabel.setText("0");
            presenciasLabel.setText("0");
            ausenciasLabel.setText("0");
            porcentajeAsistenciaLabel.setText("0%");
            return;
        }

        // Contar estadísticas
        int totalClases = listaAsistencias.size();
        long presencias = listaAsistencias.stream()
                .mapToLong(a -> a.isPresente() ? 1 : 0)
                .sum();
        long ausencias = totalClases - presencias;
        
        double porcentajeAsistencia = totalClases > 0 ? (presencias * 100.0) / totalClases : 0.0;
        
        // Actualizar labels
        totalClasesLabel.setText(String.valueOf(totalClases));
        presenciasLabel.setText(String.valueOf(presencias));
        ausenciasLabel.setText(String.valueOf(ausencias));
        porcentajeAsistenciaLabel.setText(String.format("%.1f%%", porcentajeAsistencia));
        
        // Cambiar color del porcentaje según el valor
        Color colorPorcentaje;
        if (porcentajeAsistencia >= 90) {
            colorPorcentaje = Color.web("#27ae60"); // Verde
        } else if (porcentajeAsistencia >= 80) {
            colorPorcentaje = Color.web("#f39c12"); // Naranja
        } else {
            colorPorcentaje = Color.web("#e74c3c"); // Rojo
        }
        porcentajeAsistenciaLabel.setTextFill(colorPorcentaje);
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

    private void exportarReporte() {
        // Placeholder para funcionalidad de exportar
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exportar Reporte");
        alert.setHeaderText("Funcionalidad en desarrollo");
        alert.setContentText("La exportación de reportes de asistencia estará disponible próximamente.");
        alert.showAndWait();
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
}