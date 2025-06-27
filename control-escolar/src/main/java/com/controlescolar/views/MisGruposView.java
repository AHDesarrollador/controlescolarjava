package com.controlescolar.views;

import com.controlescolar.controllers.GrupoController;
import com.controlescolar.controllers.AlumnoController;
import com.controlescolar.controllers.MateriaController;
import com.controlescolar.controllers.ProfesorController;
import com.controlescolar.models.Grupo;
import com.controlescolar.models.Alumno;
import com.controlescolar.models.Materia;
import com.controlescolar.models.Profesor;
import com.controlescolar.models.Usuario;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.bson.types.ObjectId;

import java.util.List;

public class MisGruposView extends Application {

    private Usuario usuarioActual;
    private Stage primaryStage;

    private TableView<Grupo> tablaGrupos;
    private ObservableList<Grupo> listaGrupos;
    private TextField buscarField;
    private Button btnRefrescar, btnVerAlumnos, btnVerMaterias;

    public MisGruposView(Usuario usuario) {
        this.usuarioActual = usuario;
        this.listaGrupos = FXCollections.observableArrayList();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Control Escolar - Mis Grupos");

        VBox root = new VBox();
        root.getChildren().addAll(
                createHeader(),
                createToolBar(),
                createContent()
        );

        Scene scene = new Scene(root, 1000, 600);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("No se pudo cargar el archivo CSS: " + e.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.show();

        cargarMisGrupos();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #34495e;");

        Label titleLabel = new Label("Mis Grupos");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("Grupos asignados como profesor titular");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.LIGHTGRAY);

        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }

    private HBox createToolBar() {
        HBox toolBar = new HBox(10);
        toolBar.setPadding(new Insets(15));
        toolBar.setStyle("-fx-background-color: #ecf0f1;");

        Label buscarLabel = new Label("Buscar:");
        buscarLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        buscarField = new TextField();
        buscarField.setPromptText("Buscar en mis grupos...");
        buscarField.setPrefWidth(200);
        buscarField.textProperty().addListener((obs, oldText, newText) -> filtrarGrupos(newText));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnVerAlumnos = new Button("👥 Ver Alumnos");
        btnVerAlumnos.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnVerAlumnos.setOnAction(e -> mostrarAlumnosDelGrupo());
        btnVerAlumnos.setDisable(true);

        btnVerMaterias = new Button("📚 Ver Materias");
        btnVerMaterias.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold;");
        btnVerMaterias.setOnAction(e -> mostrarMateriasDelGrupo());
        btnVerMaterias.setDisable(true);

        btnRefrescar = new Button("🔄 Refrescar");
        btnRefrescar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        btnRefrescar.setOnAction(e -> cargarMisGrupos());

        toolBar.getChildren().addAll(buscarLabel, buscarField, spacer, 
                                     btnVerAlumnos, btnVerMaterias, btnRefrescar);
        return toolBar;
    }

    private VBox createContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        tablaGrupos = createTablaGrupos();
        content.getChildren().add(tablaGrupos);
        return content;
    }

    private TableView<Grupo> createTablaGrupos() {
        TableView<Grupo> tabla = new TableView<>();
        tabla.setItems(listaGrupos);
        
        tabla.setStyle(
            "-fx-text-fill: black; " +
            "-fx-background-color: white; " +
            "-fx-control-inner-background: white; " +
            "-fx-control-inner-background-alt: #f4f4f4; " +
            "-fx-table-cell-border-color: #ddd; " +
            "-fx-table-header-border-color: #ddd;"
        );

        TableColumn<Grupo, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colCodigo.setPrefWidth(100);

        TableColumn<Grupo, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(200);

        TableColumn<Grupo, String> colGrado = new TableColumn<>("Grado");
        colGrado.setCellValueFactory(new PropertyValueFactory<>("grado"));
        colGrado.setPrefWidth(100);

        TableColumn<Grupo, String> colSeccion = new TableColumn<>("Sección");
        colSeccion.setCellValueFactory(new PropertyValueFactory<>("seccion"));
        colSeccion.setPrefWidth(100);

        TableColumn<Grupo, Integer> colAlumnos = new TableColumn<>("Alumnos");
        colAlumnos.setCellValueFactory(cellData -> {
            List<ObjectId> alumnosIds = cellData.getValue().getAlumnosIds();
            int count = alumnosIds != null ? alumnosIds.size() : 0;
            return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
        });
        colAlumnos.setPrefWidth(80);

        TableColumn<Grupo, Integer> colMaterias = new TableColumn<>("Materias");
        colMaterias.setCellValueFactory(cellData -> {
            List<ObjectId> materiasIds = cellData.getValue().getMateriasIds();
            int count = materiasIds != null ? materiasIds.size() : 0;
            return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
        });
        colMaterias.setPrefWidth(80);

        TableColumn<Grupo, String> colEstatus = new TableColumn<>("Estatus");
        colEstatus.setCellValueFactory(cellData -> {
            boolean activo = cellData.getValue().isActivo();
            return new javafx.beans.property.SimpleStringProperty(activo ? "Activo" : "Inactivo");
        });
        colEstatus.setPrefWidth(100);

        colEstatus.setCellFactory(column -> new TableCell<Grupo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Activo".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tabla.getColumns().addAll(colCodigo, colNombre, colGrado, colSeccion, 
                                  colAlumnos, colMaterias, colEstatus);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean seleccionValida = newSelection != null;
            btnVerAlumnos.setDisable(!seleccionValida);
            btnVerMaterias.setDisable(!seleccionValida);
        });

        return tabla;
    }

    private void cargarMisGrupos() {
        try {
            if (usuarioActual != null && usuarioActual.getEmail() != null) {
                Profesor profesor = ProfesorController.obtenerProfesorPorEmail(usuarioActual.getEmail());
                if (profesor != null) {
                    List<Grupo> grupos = GrupoController.obtenerGruposPorProfesor(profesor.getId());
                    listaGrupos.clear();
                    listaGrupos.addAll(grupos);
                }
            }
        } catch (Exception e) {
            mostrarError("Error al cargar mis grupos: " + e.getMessage());
        }
    }

    private void filtrarGrupos(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            cargarMisGrupos();
            return;
        }

        try {
            if (usuarioActual != null && usuarioActual.getEmail() != null) {
                Profesor profesor = ProfesorController.obtenerProfesorPorEmail(usuarioActual.getEmail());
                if (profesor != null) {
                    List<Grupo> todosLosGrupos = GrupoController.obtenerGruposPorProfesor(profesor.getId());
                    listaGrupos.clear();
                    
                    String filtroLower = filtro.toLowerCase();
                    todosLosGrupos.stream()
                        .filter(grupo -> 
                            (grupo.getCodigo() != null && grupo.getCodigo().toLowerCase().contains(filtroLower)) ||
                            (grupo.getNombre() != null && grupo.getNombre().toLowerCase().contains(filtroLower)) ||
                            (grupo.getGrado() != null && grupo.getGrado().toLowerCase().contains(filtroLower)) ||
                            (grupo.getSeccion() != null && grupo.getSeccion().toLowerCase().contains(filtroLower))
                        )
                        .forEach(listaGrupos::add);
                }
            }
        } catch (Exception e) {
            mostrarError("Error al filtrar grupos: " + e.getMessage());
        }
    }

    private void mostrarAlumnosDelGrupo() {
        Grupo grupoSeleccionado = tablaGrupos.getSelectionModel().getSelectedItem();
        if (grupoSeleccionado == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Alumnos del Grupo: " + grupoSeleccionado.getNombre());
        dialog.setHeaderText("Lista de alumnos inscritos en este grupo");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        if (grupoSeleccionado.getAlumnosIds() != null && !grupoSeleccionado.getAlumnosIds().isEmpty()) {
            ListView<String> listaAlumnos = new ListView<>();
            
            for (ObjectId alumnoId : grupoSeleccionado.getAlumnosIds()) {
                Alumno alumno = AlumnoController.obtenerAlumnoPorId(alumnoId);
                if (alumno != null) {
                    listaAlumnos.getItems().add(alumno.getNombreCompleto() + " - " + alumno.getMatricula());
                }
            }
            
            listaAlumnos.setPrefHeight(300);
            content.getChildren().addAll(
                new Label("Total de alumnos: " + grupoSeleccionado.getAlumnosIds().size()),
                listaAlumnos
            );
        } else {
            Label noAlumnos = new Label("No hay alumnos asignados a este grupo");
            noAlumnos.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            content.getChildren().add(noAlumnos);
        }

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void mostrarMateriasDelGrupo() {
        Grupo grupoSeleccionado = tablaGrupos.getSelectionModel().getSelectedItem();
        if (grupoSeleccionado == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Materias del Grupo: " + grupoSeleccionado.getNombre());
        dialog.setHeaderText("Lista de materias asignadas a este grupo");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        if (grupoSeleccionado.getMateriasIds() != null && !grupoSeleccionado.getMateriasIds().isEmpty()) {
            ListView<String> listaMaterias = new ListView<>();
            
            for (ObjectId materiaId : grupoSeleccionado.getMateriasIds()) {
                Materia materia = MateriaController.obtenerMateriaPorId(materiaId);
                if (materia != null) {
                    listaMaterias.getItems().add(materia.getNombre() + " - " + materia.getCodigo());
                }
            }
            
            listaMaterias.setPrefHeight(300);
            content.getChildren().addAll(
                new Label("Total de materias: " + grupoSeleccionado.getMateriasIds().size()),
                listaMaterias
            );
        } else {
            Label noMaterias = new Label("No hay materias asignadas a este grupo");
            noMaterias.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            content.getChildren().add(noMaterias);
        }

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}