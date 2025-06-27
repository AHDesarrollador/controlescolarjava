//AlumnosView.java
package com.controlescolar.views;

import com.controlescolar.controllers.AlumnoController;
import com.controlescolar.models.Alumno;
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
import javafx.scene.Node;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AlumnosView extends Application {

    // AlumnoController tiene métodos estáticos, no necesita instancia
    private Usuario usuarioActual;
    private Stage primaryStage;

    // Componentes UI
    private TableView<Alumno> tablaAlumnos;
    private ObservableList<Alumno> listaAlumnos;
    private TextField buscarField;
    private Button btnAgregar, btnEditar, btnEliminar, btnRefrescar;

    // Formulario
    private TextField nombreField, apellidoField, emailField, telefonoField, matriculaField;
    private DatePicker fechaNacimientoPicker;
    private TextArea direccionArea;
    private ComboBox<String> gradoCombo, grupoCombo;

    public AlumnosView(Usuario usuario) {
        this.usuarioActual = usuario;
        // AlumnoController usa métodos estáticos
        this.listaAlumnos = FXCollections.observableArrayList();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setTitle("Gestión de Alumnos");
        primaryStage.setWidth(1000);
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
        cargarAlumnos();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #2c3e50;");

        // Título
        Label titulo = new Label("Gestión de Alumnos");
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
        buscarField.setPromptText("Buscar por nombre, matrícula o email...");
        buscarField.setPrefWidth(300);
        buscarField.textProperty().addListener((obs, oldText, newText) -> filtrarAlumnos(newText));

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
        btnEliminar.setOnAction(e -> eliminarAlumno());
        btnEliminar.setDisable(true);

        btnRefrescar = new Button("🔄 Refrescar");
        btnRefrescar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        btnRefrescar.setOnAction(e -> cargarAlumnos());

        toolBar.getChildren().addAll(buscarLabel, buscarField, spacer, btnAgregar, btnEditar, btnEliminar, btnRefrescar);
        return toolBar;
    }

    private VBox createMainContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Tabla de alumnos
        tablaAlumnos = createTablaAlumnos();

        content.getChildren().add(tablaAlumnos);
        return content;
    }

    private TableView<Alumno> createTablaAlumnos() {
        TableView<Alumno> tabla = new TableView<>();
        tabla.setItems(listaAlumnos);
        
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
        TableColumn<Alumno, String> colMatricula = new TableColumn<>("Matrícula");
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colMatricula.setPrefWidth(100);

        TableColumn<Alumno, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(150);

        TableColumn<Alumno, String> colApellido = new TableColumn<>("Apellidos");
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colApellido.setPrefWidth(150);

        TableColumn<Alumno, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        TableColumn<Alumno, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTelefono.setPrefWidth(120);

        // Campos grado y grupo no existen en el modelo Alumno
        // Se comentan hasta que se definan en el modelo

        TableColumn<Alumno, LocalDate> colFechaNac = new TableColumn<>("Fecha Nac.");
        colFechaNac.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));
        colFechaNac.setPrefWidth(100);

        tabla.getColumns().addAll(colMatricula, colNombre, colApellido, colEmail, colTelefono, colFechaNac);

        // Listener para selección
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean seleccionValida = newSelection != null;
            btnEditar.setDisable(!seleccionValida);
            btnEliminar.setDisable(!seleccionValida);
        });

        // Doble click para editar
        tabla.setRowFactory(tv -> {
            TableRow<Alumno> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    mostrarFormularioEditar();
                }
            });
            return row;
        });

        return tabla;
    }

    private void cargarAlumnos() {
        try {
            List<Alumno> alumnos = AlumnoController.obtenerAlumnos();
            listaAlumnos.clear();
            listaAlumnos.addAll(alumnos);
        } catch (Exception e) {
            System.err.println("Error al cargar alumnos: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al cargar alumnos: " + e.getMessage());
        }
    }

    private void filtrarAlumnos(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            cargarAlumnos();
            return;
        }

        try {
            List<Alumno> alumnosFiltrados = AlumnoController.buscarAlumnos(filtro);
            listaAlumnos.clear();
            listaAlumnos.addAll(alumnosFiltrados);
        } catch (Exception e) {
            mostrarError("Error al filtrar alumnos: " + e.getMessage());
        }
    }

    private void mostrarFormularioAgregar() {
        mostrarFormulario("Agregar Alumno", null);
    }

    private void mostrarFormularioEditar() {
        Alumno seleccionado = tablaAlumnos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            mostrarFormulario("Editar Alumno", seleccionado);
        }
    }

    private void mostrarFormulario(String titulo, Alumno alumno) {
        Dialog<Alumno> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);

        // Configurar botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        // Crear formulario
        GridPane grid = createFormulario();

        // Si es edición, llenar campos
        if (alumno != null) {
            llenarFormulario(alumno);
        } else {
            // Generar matrícula automática para nuevo alumno
            matriculaField.setText(generarMatricula());
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
                return crearAlumnoDesdeFormulario(alumno);
            }
            return null;
        });

        Optional<Alumno> resultado = dialog.showAndWait();

        resultado.ifPresent(alumnoGuardado -> {
            try {
                if (alumno == null) {
                    // Agregar nuevo
                    AlumnoController.crearAlumno(alumnoGuardado);
                    mostrarInfo("Alumno agregado exitosamente");
                } else {
                    // Actualizar existente
                    AlumnoController.actualizarAlumno(alumnoGuardado);
                    mostrarInfo("Alumno actualizado exitosamente");
                }
                cargarAlumnos();
            } catch (Exception e) {
                mostrarError("Error al guardar alumno: " + e.getMessage());
            }
        });
    }

    private GridPane createFormulario() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Matrícula
        grid.add(new Label("Matrícula:"), 0, 0);
        matriculaField = new TextField();
        matriculaField.setDisable(true); // Matrícula automática
        grid.add(matriculaField, 1, 0);

        // Nombre
        grid.add(new Label("Nombre:"), 0, 1);
        nombreField = new TextField();
        nombreField.setPromptText("Ingresa el nombre");
        grid.add(nombreField, 1, 1);

        // Apellidos
        grid.add(new Label("Apellidos:"), 0, 2);
        apellidoField = new TextField();
        apellidoField.setPromptText("Ingresa los apellidos");
        grid.add(apellidoField, 1, 2);

        // Email
        grid.add(new Label("Email:"), 0, 3);
        emailField = new TextField();
        emailField.setPromptText("ejemplo@correo.com");
        grid.add(emailField, 1, 3);

        // Teléfono
        grid.add(new Label("Teléfono:"), 0, 4);
        telefonoField = new TextField();
        telefonoField.setPromptText("Número de teléfono");
        grid.add(telefonoField, 1, 4);

        // Fecha de nacimiento
        grid.add(new Label("Fecha Nacimiento:"), 0, 5);
        fechaNacimientoPicker = new DatePicker();
        grid.add(fechaNacimientoPicker, 1, 5);

        // Dirección
        grid.add(new Label("Dirección:"), 0, 6);
        direccionArea = new TextArea();
        direccionArea.setPrefRowCount(3);
        direccionArea.setPromptText("Dirección completa");
        grid.add(direccionArea, 1, 6);

        return grid;
    }

    private void llenarFormulario(Alumno alumno) {
        matriculaField.setText(alumno.getMatricula());
        nombreField.setText(alumno.getNombre());
        apellidoField.setText(alumno.getApellidos());
        emailField.setText(alumno.getEmail());
        telefonoField.setText(alumno.getTelefono());
        fechaNacimientoPicker.setValue(alumno.getFechaNacimiento());
        direccionArea.setText(alumno.getDireccion());
    }

    private void agregarValidacionFormulario(Node guardarButton) {
        // Lista de campos requeridos
        TextField[] camposRequeridos = {nombreField, apellidoField, emailField};

        // Listener para campos de texto
        for (TextField campo : camposRequeridos) {
            campo.textProperty().addListener((obs, oldText, newText) ->
                    validarFormulario(guardarButton, camposRequeridos));
        }

        // No hay combos requeridos por ahora

        // Listener para fecha
        fechaNacimientoPicker.valueProperty().addListener((obs, oldValue, newValue) ->
                validarFormulario(guardarButton, camposRequeridos));
    }

    private void validarFormulario(Node guardarButton, TextField[] campos) {
        boolean valido = true;

        // Validar campos de texto
        for (TextField campo : campos) {
            if (campo.getText().trim().isEmpty()) {
                valido = false;
                break;
            }
        }

        // No hay combos para validar por ahora

        // Validar fecha
        if (valido && fechaNacimientoPicker.getValue() == null) {
            valido = false;
        }

        // Validar email
        if (valido && !emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            valido = false;
        }

        guardarButton.setDisable(!valido);
    }

    private Alumno crearAlumnoDesdeFormulario(Alumno alumnoExistente) {
        Alumno alumno = alumnoExistente != null ? alumnoExistente : new Alumno();

        alumno.setMatricula(matriculaField.getText());
        alumno.setNombre(nombreField.getText().trim());
        alumno.setApellidos(apellidoField.getText().trim());
        alumno.setEmail(emailField.getText().trim());
        alumno.setTelefono(telefonoField.getText().trim());
        alumno.setFechaNacimiento(fechaNacimientoPicker.getValue());
        alumno.setDireccion(direccionArea.getText().trim());

        return alumno;
    }

    private String generarMatricula() {
        // Generar matrícula automática: año + número secuencial
        int año = LocalDate.now().getYear();
        int numero = (int) (Math.random() * 9999) + 1;
        return String.format("%d%04d", año, numero);
    }

    private void eliminarAlumno() {
        Alumno seleccionado = tablaAlumnos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Eliminación");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("¿Estás seguro que deseas eliminar al alumno: "
                + seleccionado.getNombre() + " " + seleccionado.getApellidos() + "?");

        Optional<ButtonType> resultado = confirmAlert.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                AlumnoController.eliminarAlumno(seleccionado.getId());
                mostrarInfo("Alumno eliminado exitosamente");
                cargarAlumnos();
            } catch (Exception e) {
                mostrarError("Error al eliminar alumno: " + e.getMessage());
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
}