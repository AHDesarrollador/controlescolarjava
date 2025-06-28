//MisCalificacionesView.java
package com.controlescolar.views;

import com.controlescolar.controllers.CalificacionController;
import com.controlescolar.controllers.AlumnoController;
import com.controlescolar.controllers.MateriaController;
import com.controlescolar.models.Calificacion;
import com.controlescolar.models.Alumno;
import com.controlescolar.models.Materia;
import com.controlescolar.models.Usuario;
import com.controlescolar.enums.TipoCalificacion;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MisCalificacionesView extends Application {
    private Stage stage;
    private Usuario usuarioActual;
    private Alumno alumnoActual;
    private TableView<Calificacion> tablaCalificaciones;
    private ObservableList<Calificacion> listaCalificaciones;
    private ComboBox<Materia> filtroMateriaCombo;
    private ComboBox<String> filtroPeriodoCombo;
    private ComboBox<TipoCalificacion> filtroTipoCombo;
    
    // Labels para estadísticas
    private Label promedioGeneralLabel;
    private Label totalCalificacionesLabel;
    private Label materiasMejorLabel;
    private Label estadoAcademicoLabel;

    public MisCalificacionesView(Usuario usuario) {
        this.usuarioActual = usuario;
        this.listaCalificaciones = FXCollections.observableArrayList();
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Mis Calificaciones - Sistema Control Escolar");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        root.setTop(createHeader());
        
        // Centro - contenido principal
        VBox centerContent = new VBox(15);
        centerContent.setPadding(new Insets(20));
        
        // Estadísticas del alumno
        centerContent.getChildren().add(createEstadisticasPanel());
        
        // Filtros
        centerContent.getChildren().add(createFiltrosPanel());
        
        // Tabla de calificaciones
        centerContent.getChildren().add(createTablaCalificaciones());
        
        root.setCenter(centerContent);
        
        // Footer
        root.setBottom(createFooter());
        
        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.show();
        
        // Cargar datos
        cargarDatosAlumno();
        cargarCalificaciones();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #3498db;");
        
        Label titulo = new Label("📊 Mis Calificaciones");
        titulo.setTextFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        Label subtitulo = new Label("Consulta tu historial académico y rendimiento");
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
        
        // Promedio General
        VBox promedioBox = createStatCard("📈 Promedio General", "0.0", Color.web("#3498db"));
        promedioGeneralLabel = (Label) ((VBox) promedioBox.getChildren().get(0)).getChildren().get(1);
        
        // Total Calificaciones
        VBox totalBox = createStatCard("📝 Total Calificaciones", "0", Color.web("#2ecc71"));
        totalCalificacionesLabel = (Label) ((VBox) totalBox.getChildren().get(0)).getChildren().get(1);
        
        // Mejor Materia
        VBox mejorBox = createStatCard("🏆 Mejor Materia", "N/A", Color.web("#f39c12"));
        materiasMejorLabel = (Label) ((VBox) mejorBox.getChildren().get(0)).getChildren().get(1);
        
        // Estado Académico
        VBox estadoBox = createStatCard("🎯 Estado Académico", "N/A", Color.web("#9b59b6"));
        estadoAcademicoLabel = (Label) ((VBox) estadoBox.getChildren().get(0)).getChildren().get(1);
        
        estadisticas.getChildren().addAll(promedioBox, totalBox, mejorBox, estadoBox);
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
        filtroMateriaCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrarCalificaciones());
        
        // Filtro por período
        Label periodoLabel = new Label("Período:");
        periodoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        filtroPeriodoCombo = new ComboBox<>();
        filtroPeriodoCombo.getItems().addAll("Todos", "Primer Parcial", "Segundo Parcial", "Tercer Parcial", "Final");
        filtroPeriodoCombo.setValue("Todos");
        filtroPeriodoCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrarCalificaciones());
        
        // Filtro por tipo
        Label tipoLabel = new Label("Tipo de Evaluación:");
        tipoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        filtroTipoCombo = new ComboBox<>();
        filtroTipoCombo.getItems().add(null); // "Todos"
        filtroTipoCombo.getItems().addAll(TipoCalificacion.values());
        filtroTipoCombo.setConverter(new javafx.util.StringConverter<TipoCalificacion>() {
            @Override
            public String toString(TipoCalificacion tipo) {
                return tipo == null ? "Todos los tipos" : tipo.getNombre();
            }
            @Override
            public TipoCalificacion fromString(String string) {
                return null;
            }
        });
        filtroTipoCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrarCalificaciones());
        
        // Botón actualizar
        Button btnActualizar = new Button("🔄 Actualizar");
        btnActualizar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnActualizar.setOnAction(e -> cargarCalificaciones());
        
        filtros.getChildren().addAll(materiaLabel, filtroMateriaCombo, periodoLabel, filtroPeriodoCombo, 
                tipoLabel, filtroTipoCombo, btnActualizar);
        return filtros;
    }

    private VBox createTablaCalificaciones() {
        VBox container = new VBox(10);
        
        Label tituloTabla = new Label("📋 Historial de Calificaciones");
        tituloTabla.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        tablaCalificaciones = new TableView<>();
        tablaCalificaciones.setItems(listaCalificaciones);
        
        // Estilos para mejorar la visibilidad del texto
        tablaCalificaciones.setStyle(
            "-fx-text-fill: black; " +
            "-fx-background-color: white; " +
            "-fx-control-inner-background: white; " +
            "-fx-control-inner-background-alt: #f4f4f4; " +
            "-fx-table-cell-border-color: #ddd; " +
            "-fx-table-header-border-color: #ddd;"
        );
        
        // Columnas
        TableColumn<Calificacion, String> colMateria = new TableColumn<>("Materia");
        colMateria.setCellValueFactory(cellData -> {
            Calificacion cal = cellData.getValue();
            String nombreMateria = obtenerNombreMateria(cal.getMateriaId());
            return new javafx.beans.property.SimpleStringProperty(nombreMateria);
        });
        colMateria.setPrefWidth(200);
        
        TableColumn<Calificacion, String> colTipo = new TableColumn<>("Tipo de Evaluación");
        colTipo.setCellValueFactory(cellData -> {
            TipoCalificacion tipo = cellData.getValue().getTipo();
            return new javafx.beans.property.SimpleStringProperty(tipo != null ? tipo.getNombre() : "N/A");
        });
        colTipo.setPrefWidth(150);
        
        TableColumn<Calificacion, Double> colCalificacion = new TableColumn<>("Calificación");
        colCalificacion.setCellValueFactory(new PropertyValueFactory<>("calificacion"));
        colCalificacion.setCellFactory(column -> new TableCell<Calificacion, Double>() {
            @Override
            protected void updateItem(Double calificacion, boolean empty) {
                super.updateItem(calificacion, empty);
                if (empty || calificacion == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f", calificacion));
                    // Colorear según la calificación
                    if (calificacion >= 9.0) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Verde
                    } else if (calificacion >= 7.0) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); // Naranja
                    } else if (calificacion >= 6.0) {
                        setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;"); // Azul
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Rojo
                    }
                }
            }
        });
        colCalificacion.setPrefWidth(100);
        
        TableColumn<Calificacion, String> colPeriodo = new TableColumn<>("Período");
        colPeriodo.setCellValueFactory(new PropertyValueFactory<>("periodo"));
        colPeriodo.setPrefWidth(120);
        
        TableColumn<Calificacion, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaRegistro() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getFechaRegistro().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colFecha.setPrefWidth(100);
        
        TableColumn<Calificacion, String> colObservaciones = new TableColumn<>("Observaciones");
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
        colObservaciones.setPrefWidth(200);
        
        tablaCalificaciones.getColumns().addAll(colMateria, colTipo, colCalificacion, colPeriodo, colFecha, colObservaciones);
        
        container.getChildren().addAll(tituloTabla, tablaCalificaciones);
        VBox.setVgrow(tablaCalificaciones, Priority.ALWAYS);
        
        return container;
    }

    private HBox createFooter() {
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 1px 0 0 0;");
        
        Button btnCerrar = new Button("❌ Cerrar");
        btnCerrar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        btnCerrar.setOnAction(e -> stage.close());
        
        footer.getChildren().add(btnCerrar);
        return footer;
    }

    private void cargarDatosAlumno() {
        try {
            // Buscar el alumno asociado al usuario actual
            // Asumimos que el email del usuario es el mismo que el del alumno
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

    private void cargarCalificaciones() {
        if (alumnoActual == null) return;
        
        try {
            List<Calificacion> calificaciones = CalificacionController.obtenerCalificacionesPorAlumno(alumnoActual.getId());
            listaCalificaciones.clear();
            listaCalificaciones.addAll(calificaciones);
            
            // Cargar materias para el filtro
            cargarMateriasParaFiltro();
            
            // Actualizar estadísticas
            actualizarEstadisticas();
            
        } catch (Exception e) {
            mostrarError("Error al cargar calificaciones: " + e.getMessage());
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

    private void filtrarCalificaciones() {
        // Implementar filtrado básico
        cargarCalificaciones(); // Por simplicidad, recargamos
    }

    private void actualizarEstadisticas() {
        if (listaCalificaciones.isEmpty()) {
            promedioGeneralLabel.setText("0.0");
            totalCalificacionesLabel.setText("0");
            materiasMejorLabel.setText("N/A");
            estadoAcademicoLabel.setText("Sin datos");
            return;
        }

        // Promedio general
        double promedioGeneral = CalificacionController.calcularPromedioGeneralAlumno(alumnoActual.getId());
        promedioGeneralLabel.setText(String.format("%.2f", promedioGeneral));
        
        // Total de calificaciones
        totalCalificacionesLabel.setText(String.valueOf(listaCalificaciones.size()));
        
        // Mejor materia (promedio más alto)
        Map<ObjectId, Double> promediosPorMateria = listaCalificaciones.stream()
                .collect(Collectors.groupingBy(
                        Calificacion::getMateriaId,
                        Collectors.averagingDouble(Calificacion::getCalificacion)
                ));
        
        ObjectId mejorMateriaId = promediosPorMateria.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        
        if (mejorMateriaId != null) {
            String nombreMejorMateria = obtenerNombreMateria(mejorMateriaId);
            materiasMejorLabel.setText(nombreMejorMateria);
        } else {
            materiasMejorLabel.setText("N/A");
        }
        
        // Estado académico
        String estado = CalificacionController.determinarEstadoAprobacion(promedioGeneral);
        estadoAcademicoLabel.setText(estado);
        
        // Cambiar color según el estado
        Color colorEstado;
        switch (estado) {
            case "Excelente":
                colorEstado = Color.web("#27ae60");
                break;
            case "Bueno":
                colorEstado = Color.web("#f39c12");
                break;
            case "Aprobado":
                colorEstado = Color.web("#3498db");
                break;
            default:
                colorEstado = Color.web("#e74c3c");
                break;
        }
        estadoAcademicoLabel.setTextFill(colorEstado);
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