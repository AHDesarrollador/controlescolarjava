//ProfesoresView.java
package com.controlescolar.views;

import com.controlescolar.controllers.ProfesorController;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ProfesoresView extends Application {

    private Usuario usuarioActual;
    private Stage primaryStage;

    // Componentes UI
    private TableView<Profesor> tablaProfesores;
    private ObservableList<Profesor> listaProfesores;
    private TextField buscarField;
    private Button btnAgregar, btnEditar, btnEliminar, btnRefrescar;

    // Formulario
    private TextField nombreField, apellidoField, emailField, telefonoField, cedulaField;
    private TextArea direccionArea, especialidadesArea;
    private DatePicker fechaIngresoDatePicker;
    private ComboBox<String> estatusCombo;

    public ProfesoresView(Usuario usuario) {
        this.usuarioActual = usuario;
        this.listaProfesores = FXCollections.observableArrayList();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setTitle("Gestión de Profesores");
        primaryStage.setWidth(1100);
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
        cargarProfesores();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #2c3e50;");

        // Título
        Label titulo = new Label("Gestión de Profesores");
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
        buscarField.setPromptText("Buscar por nombre, cédula o email...");
        buscarField.setPrefWidth(300);
        buscarField.textProperty().addListener((obs, oldText, newText) -> filtrarProfesores(newText));

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
        btnEliminar.setOnAction(e -> eliminarProfesor());
        btnEliminar.setDisable(true);

        btnRefrescar = new Button("🔄 Refrescar");
        btnRefrescar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        btnRefrescar.setOnAction(e -> cargarProfesores());

        toolBar.getChildren().addAll(buscarLabel, buscarField, spacer, btnAgregar, btnEditar, btnEliminar, btnRefrescar);
        return toolBar;
    }

    private VBox createMainContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Tabla de profesores
        tablaProfesores = createTablaProfesores();

        content.getChildren().add(tablaProfesores);
        return content;
    }

    private TableView<Profesor> createTablaProfesores() {
        TableView<Profesor> tabla = new TableView<>();
        tabla.setItems(listaProfesores);
        
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
        TableColumn<Profesor, String> colCedula = new TableColumn<>("Cédula");
        colCedula.setCellValueFactory(new PropertyValueFactory<>("numeroEmpleado"));
        colCedula.setPrefWidth(120);

        TableColumn<Profesor, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(150);

        TableColumn<Profesor, String> colApellido = new TableColumn<>("Apellido");
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colApellido.setPrefWidth(150);

        TableColumn<Profesor, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        TableColumn<Profesor, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTelefono.setPrefWidth(120);

        TableColumn<Profesor, String> colEspecialidades = new TableColumn<>("Especialidades");
        colEspecialidades.setCellValueFactory(new PropertyValueFactory<>("especialidad"));
        colEspecialidades.setPrefWidth(180);

        TableColumn<Profesor, LocalDate> colFechaIngreso = new TableColumn<>("Fecha Ingreso");
        colFechaIngreso.setCellValueFactory(new PropertyValueFactory<>("fechaIngreso"));
        colFechaIngreso.setPrefWidth(120);

        TableColumn<Profesor, String> colEstatus = new TableColumn<>("Estatus");
        colEstatus.setCellValueFactory(cellData -> {
            boolean activo = cellData.getValue().isActivo();
            return new javafx.beans.property.SimpleStringProperty(activo ? "Activo" : "Inactivo");
        });
        colEstatus.setPrefWidth(100);

        // Colorear celda de estatus
        colEstatus.setCellFactory(column -> new TableCell<Profesor, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Activo".equals(item)) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                    } else if ("Inactivo".equals(item)) {
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                    }
                }
            }
        });

        tabla.getColumns().addAll(colCedula, colNombre, colApellido, colEmail, colTelefono, colEspecialidades, colFechaIngreso, colEstatus);

        // Listener para selección
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean seleccionValida = newSelection != null;
            btnEditar.setDisable(!seleccionValida);
            btnEliminar.setDisable(!seleccionValida);
        });

        // Doble click para editar
        tabla.setRowFactory(tv -> {
            TableRow<Profesor> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    mostrarFormularioEditar();
                }
            });
            return row;
        });

        return tabla;
    }

    private void cargarProfesores() {
        try {
            List<Profesor> profesores = ProfesorController.obtenerProfesores();
            listaProfesores.clear();
            listaProfesores.addAll(profesores);
        } catch (Exception e) {
            mostrarError("Error al cargar profesores: " + e.getMessage());
        }
    }

    private void filtrarProfesores(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            cargarProfesores();
            return;
        }

        try {
            List<Profesor> profesoresFiltrados = ProfesorController.buscarProfesores(filtro);
            listaProfesores.clear();
            listaProfesores.addAll(profesoresFiltrados);
        } catch (Exception e) {
            mostrarError("Error al filtrar profesores: " + e.getMessage());
        }
    }

    private void mostrarFormularioAgregar() {
        mostrarFormulario("Agregar Profesor", null);
    }

    private void mostrarFormularioEditar() {
        Profesor seleccionado = tablaProfesores.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            mostrarFormulario("Editar Profesor", seleccionado);
        }
    }

    private void mostrarFormulario(String titulo, Profesor profesor) {
        Dialog<Profesor> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);

        // Configurar botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        // Crear formulario
        GridPane grid = createFormulario();

        // Si es edición, llenar campos
        if (profesor != null) {
            llenarFormulario(profesor);
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
                return crearProfesorDesdeFormulario(profesor);
            }
            return null;
        });

        Optional<Profesor> resultado = dialog.showAndWait();

        resultado.ifPresent(profesorGuardado -> {
            try {
                if (profesor == null) {
                    // Agregar nuevo
                    ProfesorController.crearProfesor(profesorGuardado);
                    mostrarInfo("Profesor agregado exitosamente");
                } else {
                    // Actualizar existente
                    ProfesorController.actualizarProfesor(profesorGuardado);
                    mostrarInfo("Profesor actualizado exitosamente");
                }
                cargarProfesores();
            } catch (Exception e) {
                mostrarError("Error al guardar profesor: " + e.getMessage());
            }
        });
    }

    private GridPane createFormulario() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Cédula
        grid.add(new Label("Cédula:"), 0, 0);
        cedulaField = new TextField();
        cedulaField.setPromptText("Ingrese la cédula");
        grid.add(cedulaField, 1, 0);

        // Nombre
        grid.add(new Label("Nombre:"), 0, 1);
        nombreField = new TextField();
        nombreField.setPromptText("Ingrese el nombre");
        grid.add(nombreField, 1, 1);

        // Apellido
        grid.add(new Label("Apellido:"), 0, 2);
        apellidoField = new TextField();
        apellidoField.setPromptText("Ingrese el apellido");
        grid.add(apellidoField, 1, 2);

        // Email
        grid.add(new Label("Email:"), 0, 3);
        emailField = new TextField();
        emailField.setPromptText("Ingrese el email");
        grid.add(emailField, 1, 3);

        // Teléfono
        grid.add(new Label("Teléfono:"), 0, 4);
        telefonoField = new TextField();
        telefonoField.setPromptText("Ingrese el teléfono");
        grid.add(telefonoField, 1, 4);

        // Dirección
        grid.add(new Label("Dirección:"), 0, 5);
        direccionArea = new TextArea();
        direccionArea.setPromptText("Ingrese la dirección");
        direccionArea.setPrefRowCount(2);
        grid.add(direccionArea, 1, 5);

        // Especialidades
        grid.add(new Label("Especialidades:"), 0, 6);
        especialidadesArea = new TextArea();
        especialidadesArea.setPromptText("Ingrese las especialidades separadas por comas");
        especialidadesArea.setPrefRowCount(2);
        grid.add(especialidadesArea, 1, 6);

        // Fecha de ingreso
        grid.add(new Label("Fecha Ingreso:"), 0, 7);
        fechaIngresoDatePicker = new DatePicker();
        fechaIngresoDatePicker.setValue(LocalDate.now());
        grid.add(fechaIngresoDatePicker, 1, 7);

        // Estatus
        grid.add(new Label("Estatus:"), 0, 8);
        estatusCombo = new ComboBox<>();
        estatusCombo.getItems().addAll("Activo", "Inactivo");
        estatusCombo.setValue("Activo");
        grid.add(estatusCombo, 1, 8);

        return grid;
    }

    private void llenarFormulario(Profesor profesor) {
        // Map database fields to UI fields with null checks
        if (cedulaField != null) {
            String cedula = profesor.getNumeroEmpleado() != null ? profesor.getNumeroEmpleado() : 
                           (profesor.getCedula() != null ? profesor.getCedula() : "");
            cedulaField.setText(cedula);
        }
        if (nombreField != null) nombreField.setText(profesor.getNombre() != null ? profesor.getNombre() : "");
        if (apellidoField != null) {
            String apellidos = profesor.getApellidos() != null ? profesor.getApellidos() : 
                              (profesor.getApellido() != null ? profesor.getApellido() : "");
            apellidoField.setText(apellidos);
        }
        if (emailField != null) emailField.setText(profesor.getEmail() != null ? profesor.getEmail() : "");
        if (telefonoField != null) telefonoField.setText(profesor.getTelefono() != null ? profesor.getTelefono() : "");
        if (direccionArea != null) direccionArea.setText(profesor.getDireccion() != null ? profesor.getDireccion() : "");
        if (especialidadesArea != null) {
            String especialidad = profesor.getEspecialidad() != null ? profesor.getEspecialidad() : 
                                 (profesor.getEspecialidades() != null ? profesor.getEspecialidades() : "");
            especialidadesArea.setText(especialidad);
        }
        if (fechaIngresoDatePicker != null && profesor.getFechaIngreso() != null) {
            fechaIngresoDatePicker.setValue(profesor.getFechaIngreso().toLocalDate());
        }
        if (estatusCombo != null) estatusCombo.setValue(profesor.isActivo() ? "Activo" : "Inactivo");
    }

    private void agregarValidacionFormulario(Node guardarButton) {
        // Listener para validar campos requeridos
        Runnable validar = () -> {
            boolean valido = !cedulaField.getText().trim().isEmpty() &&
                    !nombreField.getText().trim().isEmpty() &&
                    !apellidoField.getText().trim().isEmpty() &&
                    !emailField.getText().trim().isEmpty() &&
                    fechaIngresoDatePicker.getValue() != null &&
                    estatusCombo.getValue() != null;
            guardarButton.setDisable(!valido);
        };

        cedulaField.textProperty().addListener((obs, oldVal, newVal) -> validar.run());
        nombreField.textProperty().addListener((obs, oldVal, newVal) -> validar.run());
        apellidoField.textProperty().addListener((obs, oldVal, newVal) -> validar.run());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validar.run());
        fechaIngresoDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validar.run());
        estatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> validar.run());
    }

    private Profesor crearProfesorDesdeFormulario(Profesor profesorExistente) {
        Profesor profesor = profesorExistente != null ? profesorExistente : new Profesor();

        // Map UI fields to database fields with null checks
        profesor.setNumeroEmpleado(cedulaField != null && cedulaField.getText() != null ? cedulaField.getText().trim() : "");
        profesor.setNombre(nombreField != null && nombreField.getText() != null ? nombreField.getText().trim() : "");
        profesor.setApellidos(apellidoField != null && apellidoField.getText() != null ? apellidoField.getText().trim() : "");
        profesor.setEmail(emailField != null && emailField.getText() != null ? emailField.getText().trim() : "");
        profesor.setTelefono(telefonoField != null && telefonoField.getText() != null ? telefonoField.getText().trim() : "");
        profesor.setEspecialidad(especialidadesArea != null && especialidadesArea.getText() != null ? especialidadesArea.getText().trim() : "");
        
        if (fechaIngresoDatePicker != null && fechaIngresoDatePicker.getValue() != null) {
            profesor.setFechaIngreso(fechaIngresoDatePicker.getValue().atStartOfDay());
        }
        
        profesor.setActivo("Activo".equals(estatusCombo != null ? estatusCombo.getValue() : "Activo"));
        
        // Also set UI compatibility fields
        profesor.setCedula(cedulaField != null && cedulaField.getText() != null ? cedulaField.getText().trim() : "");
        profesor.setDireccion(direccionArea != null && direccionArea.getText() != null ? direccionArea.getText().trim() : "");
        profesor.setEspecialidades(especialidadesArea != null && especialidadesArea.getText() != null ? especialidadesArea.getText().trim() : "");
        profesor.setEstatus(estatusCombo != null ? estatusCombo.getValue() : "Activo");

        return profesor;
    }

    private void eliminarProfesor() {
        Profesor seleccionado = tablaProfesores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar este profesor?");
        confirmacion.setContentText("Profesor: " + seleccionado.getNombre() + " " + seleccionado.getApellido());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                ProfesorController.eliminarProfesor(seleccionado.getId());
                mostrarInfo("Profesor eliminado exitosamente");
                cargarProfesores();
            } catch (Exception e) {
                mostrarError("Error al eliminar profesor: " + e.getMessage());
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