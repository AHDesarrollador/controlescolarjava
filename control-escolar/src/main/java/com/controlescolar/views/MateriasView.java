//MateriasView.java
package com.controlescolar.views;

import com.controlescolar.controllers.MateriaController;
import com.controlescolar.controllers.ProfesorController;
import com.controlescolar.models.Materia;
import com.controlescolar.models.Profesor;
import com.controlescolar.models.Usuario;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.util.List;
import java.util.Optional;

public class MateriasView extends Application {

    private Usuario usuarioActual;
    private Stage primaryStage;

    // Componentes UI
    private TableView<Materia> tablaMaterias;
    private ObservableList<Materia> listaMaterias;
    private TextField buscarField;
    private Button btnAgregar, btnEditar, btnEliminar, btnRefrescar;

    // Formulario
    private TextField codigoField, nombreField, creditosField, horasSemanalesField;
    private TextArea descripcionArea, prerrequisitosArea;
    private ComboBox<Profesor> profesorCombo;
    private ComboBox<String> estatusCombo, semestreCombo;

    public MateriasView(Usuario usuario) {
        this.usuarioActual = usuario;
        this.listaMaterias = FXCollections.observableArrayList();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setTitle("Gestión de Materias");
        primaryStage.setWidth(1200);
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
        cargarMaterias();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #2c3e50;");

        // Título
        Label titulo = new Label("Gestión de Materias");
        titulo.setTextFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Barra de búsqueda y botones
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
        buscarField.setPromptText("Buscar por código o nombre...");
        buscarField.setPrefWidth(300);
        buscarField.textProperty().addListener((obs, oldText, newText) -> filtrarMaterias(newText));

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
        btnEliminar.setOnAction(e -> eliminarMateria());
        btnEliminar.setDisable(true);

        btnRefrescar = new Button("🔄 Refrescar");
        btnRefrescar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        btnRefrescar.setOnAction(e -> cargarMaterias());

        toolBar.getChildren().addAll(buscarLabel, buscarField, spacer, btnAgregar, btnEditar, btnEliminar, btnRefrescar);
        return toolBar;
    }

    private VBox createMainContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Tabla de materias
        tablaMaterias = createTablaMaterias();

        content.getChildren().add(tablaMaterias);
        return content;
    }

    private TableView<Materia> createTablaMaterias() {
        TableView<Materia> tabla = new TableView<>();
        tabla.setItems(listaMaterias);
        
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
        TableColumn<Materia, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colCodigo.setPrefWidth(100);

        TableColumn<Materia, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(200);

        TableColumn<Materia, String> colDescripcion = new TableColumn<>("Descripción");
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colDescripcion.setPrefWidth(250);

        TableColumn<Materia, Integer> colCreditos = new TableColumn<>("Créditos");
        colCreditos.setCellValueFactory(new PropertyValueFactory<>("creditos"));
        colCreditos.setPrefWidth(80);

        TableColumn<Materia, Integer> colHorasSemanales = new TableColumn<>("Horas/Semana");
        colHorasSemanales.setCellValueFactory(new PropertyValueFactory<>("horasSemanales"));
        colHorasSemanales.setPrefWidth(120);

        TableColumn<Materia, String> colSemestre = new TableColumn<>("Semestre");
        colSemestre.setCellValueFactory(new PropertyValueFactory<>("semestre"));
        colSemestre.setPrefWidth(100);

        TableColumn<Materia, String> colProfesor = new TableColumn<>("Profesor");
        colProfesor.setCellValueFactory(cellData -> {
            Materia materia = cellData.getValue();
            String nombreCompleto = materia.getProfesor() != null ?
                    materia.getProfesor().getNombre() + " " + materia.getProfesor().getApellido() :
                    "Sin asignar";
            return new javafx.beans.property.SimpleStringProperty(nombreCompleto);
        });
        colProfesor.setPrefWidth(150);

        TableColumn<Materia, String> colEstatus = new TableColumn<>("Estatus");
        colEstatus.setCellValueFactory(new PropertyValueFactory<>("estatus"));
        colEstatus.setPrefWidth(100);

        // Colorear celda de estatus
        colEstatus.setCellFactory(column -> new TableCell<Materia, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Activa".equals(item)) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                    } else if ("Inactiva".equals(item)) {
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                    }
                }
            }
        });

        tabla.getColumns().addAll(colCodigo, colNombre, colDescripcion, colCreditos,
                colHorasSemanales, colSemestre, colProfesor, colEstatus);

        // Listener para selección
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean seleccionValida = newSelection != null;
            btnEditar.setDisable(!seleccionValida);
            btnEliminar.setDisable(!seleccionValida);
        });

        // Doble click para editar
        tabla.setRowFactory(tv -> {
            TableRow<Materia> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    mostrarFormularioEditar();
                }
            });
            return row;
        });

        return tabla;
    }

    private void cargarMaterias() {
        try {
            List<Materia> materias = MateriaController.obtenerMaterias();
            listaMaterias.clear();
            listaMaterias.addAll(materias);
        } catch (Exception e) {
            mostrarError("Error al cargar materias: " + e.getMessage());
        }
    }

    private void filtrarMaterias(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            cargarMaterias();
            return;
        }

        try {
            List<Materia> materiasFiltradas = MateriaController.buscarMaterias(filtro);
            listaMaterias.clear();
            listaMaterias.addAll(materiasFiltradas);
        } catch (Exception e) {
            mostrarError("Error al filtrar materias: " + e.getMessage());
        }
    }

    private void mostrarFormularioAgregar() {
        mostrarFormulario("Agregar Materia", null);
    }

    private void mostrarFormularioEditar() {
        Materia seleccionada = tablaMaterias.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            mostrarFormulario("Editar Materia", seleccionada);
        }
    }

    private void mostrarFormulario(String titulo, Materia materia) {
        Dialog<Materia> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);

        // Configurar botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        // Crear formulario
        GridPane grid = createFormulario();

        // Si es edición, llenar campos
        if (materia != null) {
            llenarFormulario(materia);
        }

        dialog.getDialogPane().setContent(grid);

        // Validación
        Node guardarButton = dialog.getDialogPane().lookupButton(guardarButtonType);
        guardarButton.setDisable(true);

        // Listener para habilitar/deshabilitar botón guardar
        agregarValidacionFormulario(guardarButton);

        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                return crearMateriaDesdeFormulario(materia);
            }
            return null;
        });

        Optional<Materia> resultado = dialog.showAndWait();

        resultado.ifPresent(materiaGuardada -> {
            try {
                if (materia == null) {
                    // Agregar nueva
                    MateriaController.crearMateria(materiaGuardada);
                    mostrarInfo("Materia agregada exitosamente");
                } else {
                    // Actualizar existente
                    MateriaController.actualizarMateria(materiaGuardada);
                    mostrarInfo("Materia actualizada exitosamente");
                }
                cargarMaterias();
            } catch (Exception e) {
                mostrarError("Error al guardar materia: " + e.getMessage());
            }
        });
    }

    private GridPane createFormulario() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Código
        grid.add(new Label("Código:"), 0, 0);
        codigoField = new TextField();
        codigoField.setPromptText("Ingrese el código de la materia");
        grid.add(codigoField, 1, 0);

        // Nombre
        grid.add(new Label("Nombre:"), 0, 1);
        nombreField = new TextField();
        nombreField.setPromptText("Ingrese el nombre de la materia");
        grid.add(nombreField, 1, 1);

        // Descripción
        grid.add(new Label("Descripción:"), 0, 2);
        descripcionArea = new TextArea();
        descripcionArea.setPromptText("Ingrese la descripción de la materia");
        descripcionArea.setPrefRowCount(3);
        grid.add(descripcionArea, 1, 2);

        // Créditos
        grid.add(new Label("Créditos:"), 0, 3);
        creditosField = new TextField();
        creditosField.setPromptText("Número de créditos");
        grid.add(creditosField, 1, 3);

        // Horas semanales
        grid.add(new Label("Horas/Semana:"), 0, 4);
        horasSemanalesField = new TextField();
        horasSemanalesField.setPromptText("Horas por semana");
        grid.add(horasSemanalesField, 1, 4);

        // Semestre
        grid.add(new Label("Semestre:"), 0, 5);
        semestreCombo = new ComboBox<>();
        semestreCombo.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        grid.add(semestreCombo, 1, 5);

        // Prerequisitos
        grid.add(new Label("Prerequisitos:"), 0, 6);
        prerrequisitosArea = new TextArea();
        prerrequisitosArea.setPromptText("Materias prerequisito (separadas por comas)");
        prerrequisitosArea.setPrefRowCount(2);
        grid.add(prerrequisitosArea, 1, 6);

        // Profesor
        grid.add(new Label("Profesor:"), 0, 7);
        profesorCombo = new ComboBox<>();
        cargarProfesores();
        grid.add(profesorCombo, 1, 7);

        // Estatus
        grid.add(new Label("Estatus:"), 0, 8);
        estatusCombo = new ComboBox<>();
        estatusCombo.getItems().addAll("Activa", "Inactiva");
        estatusCombo.setValue("Activa");
        grid.add(estatusCombo, 1, 8);

        return grid;
    }

    private void cargarProfesores() {
        try {
            List<Profesor> profesores = ProfesorController.obtenerProfesores();
            profesorCombo.getItems().clear();
            profesorCombo.getItems().add(null); // Opción "Sin asignar"
            profesorCombo.getItems().addAll(profesores);

            // Configurar StringConverter para mostrar nombre completo
            profesorCombo.setConverter(new StringConverter<Profesor>() {
                @Override
                public String toString(Profesor profesor) {
                    return profesor == null ? "Sin asignar" :
                            profesor.getNombre() + " " + profesor.getApellido();
                }

                @Override
                public Profesor fromString(String string) {
                    return null; // No necesario para este caso
                }
            });
        } catch (Exception e) {
            mostrarError("Error al cargar profesores: " + e.getMessage());
        }
    }

    private void llenarFormulario(Materia materia) {
        if (codigoField != null) codigoField.setText(materia.getCodigo() != null ? materia.getCodigo() : "");
        if (nombreField != null) nombreField.setText(materia.getNombre() != null ? materia.getNombre() : "");
        if (descripcionArea != null) descripcionArea.setText(materia.getDescripcion() != null ? materia.getDescripcion() : "");
        if (creditosField != null) creditosField.setText(String.valueOf(materia.getCreditos()));
        if (horasSemanalesField != null) horasSemanalesField.setText(String.valueOf(materia.getHorasSemanales()));
        if (semestreCombo != null) semestreCombo.setValue(materia.getSemestre() != null ? materia.getSemestre() : "");
        if (prerrequisitosArea != null) prerrequisitosArea.setText(materia.getPrerequisitos() != null ? materia.getPrerequisitos() : "");
        if (profesorCombo != null) profesorCombo.setValue(materia.getProfesor());
        if (estatusCombo != null) estatusCombo.setValue(materia.getEstatus() != null ? materia.getEstatus() : "Activo");
    }

    private void agregarValidacionFormulario(Node guardarButton) {
        // Listener para validar campos requeridos
        Runnable validar = () -> {
            boolean valido = !codigoField.getText().trim().isEmpty() &&
                    !nombreField.getText().trim().isEmpty() &&
                    !creditosField.getText().trim().isEmpty() &&
                    !horasSemanalesField.getText().trim().isEmpty() &&
                    semestreCombo.getValue() != null &&
                    estatusCombo.getValue() != null;
            guardarButton.setDisable(!valido);
        };

        codigoField.textProperty().addListener((obs, oldVal, newVal) -> validar.run());
        nombreField.textProperty().addListener((obs, oldVal, newVal) -> validar.run());
        creditosField.textProperty().addListener((obs, oldVal, newVal) -> validar.run());
        horasSemanalesField.textProperty().addListener((obs, oldVal, newVal) -> validar.run());
        semestreCombo.valueProperty().addListener((obs, oldVal, newVal) -> validar.run());
        estatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> validar.run());
    }

    private Materia crearMateriaDesdeFormulario(Materia materiaExistente) {
        Materia materia = materiaExistente != null ? materiaExistente : new Materia();

        materia.setCodigo(codigoField != null && codigoField.getText() != null ? codigoField.getText().trim() : "");
        materia.setNombre(nombreField != null && nombreField.getText() != null ? nombreField.getText().trim() : "");
        materia.setDescripcion(descripcionArea != null && descripcionArea.getText() != null ? descripcionArea.getText().trim() : "");
        
        // Manejar campos numéricos con validación
        if (creditosField != null && creditosField.getText() != null && !creditosField.getText().trim().isEmpty()) {
            try {
                materia.setCreditos(Integer.parseInt(creditosField.getText().trim()));
            } catch (NumberFormatException e) {
                materia.setCreditos(0);
            }
        }
        
        if (horasSemanalesField != null && horasSemanalesField.getText() != null && !horasSemanalesField.getText().trim().isEmpty()) {
            try {
                materia.setHorasSemanales(Integer.parseInt(horasSemanalesField.getText().trim()));
            } catch (NumberFormatException e) {
                materia.setHorasSemanales(0);
            }
        }
        
        materia.setSemestre(semestreCombo != null ? semestreCombo.getValue() : "");
        materia.setPrerequisitos(prerrequisitosArea != null && prerrequisitosArea.getText() != null ? prerrequisitosArea.getText().trim() : "");
        materia.setProfesor(profesorCombo != null ? profesorCombo.getValue() : null);
        materia.setEstatus(estatusCombo != null ? estatusCombo.getValue() : "Activo");

        return materia;
    }

    private void eliminarMateria() {
        Materia seleccionada = tablaMaterias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar esta materia?");
        confirmacion.setContentText("Materia: " + seleccionada.getCodigo() + " - " + seleccionada.getNombre());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                MateriaController.eliminarMateria(seleccionada.getId());
                mostrarInfo("Materia eliminada exitosamente");
                cargarMaterias();
            } catch (Exception e) {
                mostrarError("Error al eliminar materia: " + e.getMessage());
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

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}