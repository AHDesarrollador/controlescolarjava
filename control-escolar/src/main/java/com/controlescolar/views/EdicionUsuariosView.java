package com.controlescolar.views;

import com.controlescolar.controllers.AuthController;
import com.controlescolar.controllers.UsuarioController;
import com.controlescolar.controllers.AlumnoController;
import com.controlescolar.controllers.ProfesorController;
import com.controlescolar.models.Usuario;
import com.controlescolar.models.Alumno;
import com.controlescolar.models.Profesor;
import com.controlescolar.enums.Rol;

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
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;
import java.util.stream.Collectors;

public class EdicionUsuariosView {
    private Stage primaryStage;
    private TabPane tabPane;
    private TableView<Usuario> usuariosTable;
    private TableView<Alumno> alumnosTable;
    private TableView<Profesor> profesoresTable;
    private ObservableList<Usuario> usuariosData;
    private ObservableList<Alumno> alumnosData;
    private ObservableList<Profesor> profesoresData;

    public EdicionUsuariosView(Stage parentStage) {
        this.primaryStage = new Stage();
        this.primaryStage.initModality(Modality.WINDOW_MODAL);
        this.primaryStage.initOwner(parentStage);
        initializeView();
    }

    private void initializeView() {
        // Determinar el título según el rol
        String titulo = "Edición de Usuarios";
        if (AuthController.getUsuarioActual().getRol() == Rol.SECRETARIO) {
            titulo += " - Secretaría";
        } else if (AuthController.getUsuarioActual().getRol() == Rol.DIRECTOR) {
            titulo += " - Dirección";
        }
        
        primaryStage.setTitle(titulo);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));

        // Header
        HBox header = createHeader();

        // Tabs para diferentes tipos de usuarios
        tabPane = createTabPane();

        mainLayout.getChildren().addAll(header, tabPane);

        Scene scene = new Scene(mainLayout);
        primaryStage.setScene(scene);

        cargarTodosLosDatos();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Edición de Usuarios");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.DARKBLUE);

        header.getChildren().add(titleLabel);
        return header;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();

        // Tab de Padres de Familia
        Tab padresTab = new Tab("Padres de Familia");
        padresTab.setClosable(false);
        padresTab.setContent(createPadresContent());

        // Tab de Alumnos
        Tab alumnosTab = new Tab("Alumnos");
        alumnosTab.setClosable(false);
        alumnosTab.setContent(createAlumnosContent());

        // Tab de Profesores
        Tab profesoresTab = new Tab("Profesores");
        profesoresTab.setClosable(false);
        profesoresTab.setContent(createProfesoresContent());

        tabPane.getTabs().addAll(padresTab, alumnosTab, profesoresTab);
        return tabPane;
    }

    private VBox createPadresContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Botones de acción
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        Button editarBtn = new Button("Editar Padre");
        editarBtn.setOnAction(e -> editarUsuarioSeleccionado());

        Button activarBtn = new Button("Activar/Desactivar");
        activarBtn.setOnAction(e -> toggleActivacionUsuario());

        Button actualizarBtn = new Button("Actualizar");
        actualizarBtn.setOnAction(e -> cargarPadres());

        buttonBar.getChildren().addAll(editarBtn, activarBtn, actualizarBtn);

        // Tabla de usuarios padres
        usuariosTable = createUsuariosTable();

        content.getChildren().addAll(buttonBar, usuariosTable);
        return content;
    }

    private VBox createAlumnosContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Botones de acción
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        Button editarBtn = new Button("Editar Alumno");
        editarBtn.setOnAction(e -> editarAlumnoSeleccionado());

        Button activarBtn = new Button("Activar/Desactivar");
        activarBtn.setOnAction(e -> toggleActivacionAlumno());

        Button actualizarBtn = new Button("Actualizar");
        actualizarBtn.setOnAction(e -> cargarAlumnos());

        buttonBar.getChildren().addAll(editarBtn, activarBtn, actualizarBtn);

        // Tabla de alumnos
        alumnosTable = createAlumnosTable();

        content.getChildren().addAll(buttonBar, alumnosTable);
        return content;
    }

    private VBox createProfesoresContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Botones de acción
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        Button editarBtn = new Button("Editar Profesor");
        editarBtn.setOnAction(e -> editarProfesorSeleccionado());

        Button activarBtn = new Button("Activar/Desactivar");
        activarBtn.setOnAction(e -> toggleActivacionProfesor());

        Button actualizarBtn = new Button("Actualizar");
        actualizarBtn.setOnAction(e -> cargarProfesores());

        buttonBar.getChildren().addAll(editarBtn, activarBtn, actualizarBtn);

        // Tabla de profesores
        profesoresTable = createProfesoresTable();

        content.getChildren().addAll(buttonBar, profesoresTable);
        return content;
    }

    private TableView<Usuario> createUsuariosTable() {
        TableView<Usuario> table = new TableView<>();
        table.setPrefHeight(600);
        
        // Estilos para mejorar la visibilidad del texto
        table.setStyle(
            "-fx-text-fill: black; " +
            "-fx-background-color: white; " +
            "-fx-control-inner-background: white; " +
            "-fx-control-inner-background-alt: #f4f4f4; " +
            "-fx-table-cell-border-color: #ddd; " +
            "-fx-table-header-border-color: #ddd;"
        );

        TableColumn<Usuario, String> nombreCol = new TableColumn<>("Nombre");
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        nombreCol.setPrefWidth(150);

        TableColumn<Usuario, String> apellidosCol = new TableColumn<>("Apellidos");
        apellidosCol.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        apellidosCol.setPrefWidth(150);

        TableColumn<Usuario, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<Usuario, String> telefonoCol = new TableColumn<>("Teléfono");
        telefonoCol.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        telefonoCol.setPrefWidth(120);

        TableColumn<Usuario, String> activoCol = new TableColumn<>("Estado");
        activoCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isActivo() ? "Activo" : "Inactivo"
            )
        );
        activoCol.setPrefWidth(80);

        table.getColumns().addAll(nombreCol, apellidosCol, emailCol, telefonoCol, activoCol);

        usuariosData = FXCollections.observableArrayList();
        table.setItems(usuariosData);

        return table;
    }

    private TableView<Alumno> createAlumnosTable() {
        TableView<Alumno> table = new TableView<>();
        table.setPrefHeight(600);
        
        // Estilos para mejorar la visibilidad del texto
        table.setStyle(
            "-fx-text-fill: black; " +
            "-fx-background-color: white; " +
            "-fx-control-inner-background: white; " +
            "-fx-control-inner-background-alt: #f4f4f4; " +
            "-fx-table-cell-border-color: #ddd; " +
            "-fx-table-header-border-color: #ddd;"
        );

        TableColumn<Alumno, String> matriculaCol = new TableColumn<>("Matrícula");
        matriculaCol.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        matriculaCol.setPrefWidth(100);

        TableColumn<Alumno, String> nombreCol = new TableColumn<>("Nombre");
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        nombreCol.setPrefWidth(150);

        TableColumn<Alumno, String> apellidosCol = new TableColumn<>("Apellidos");
        apellidosCol.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        apellidosCol.setPrefWidth(150);

        TableColumn<Alumno, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<Alumno, String> telefonoCol = new TableColumn<>("Teléfono");
        telefonoCol.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        telefonoCol.setPrefWidth(120);

        TableColumn<Alumno, String> activoCol = new TableColumn<>("Estado");
        activoCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isActivo() ? "Activo" : "Inactivo"
            )
        );
        activoCol.setPrefWidth(80);

        table.getColumns().addAll(matriculaCol, nombreCol, apellidosCol, emailCol, telefonoCol, activoCol);

        alumnosData = FXCollections.observableArrayList();
        table.setItems(alumnosData);

        return table;
    }

    private TableView<Profesor> createProfesoresTable() {
        TableView<Profesor> table = new TableView<>();
        table.setPrefHeight(600);
        
        // Estilos para mejorar la visibilidad del texto
        table.setStyle(
            "-fx-text-fill: black; " +
            "-fx-background-color: white; " +
            "-fx-control-inner-background: white; " +
            "-fx-control-inner-background-alt: #f4f4f4; " +
            "-fx-table-cell-border-color: #ddd; " +
            "-fx-table-header-border-color: #ddd;"
        );

        TableColumn<Profesor, String> numeroEmpleadoCol = new TableColumn<>("No. Empleado");
        numeroEmpleadoCol.setCellValueFactory(new PropertyValueFactory<>("numeroEmpleado"));
        numeroEmpleadoCol.setPrefWidth(120);

        TableColumn<Profesor, String> nombreCol = new TableColumn<>("Nombre");
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        nombreCol.setPrefWidth(150);

        TableColumn<Profesor, String> apellidosCol = new TableColumn<>("Apellidos");
        apellidosCol.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        apellidosCol.setPrefWidth(150);

        TableColumn<Profesor, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<Profesor, String> telefonoCol = new TableColumn<>("Teléfono");
        telefonoCol.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        telefonoCol.setPrefWidth(120);

        TableColumn<Profesor, String> activoCol = new TableColumn<>("Estado");
        activoCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isActivo() ? "Activo" : "Inactivo"
            )
        );
        activoCol.setPrefWidth(80);

        table.getColumns().addAll(numeroEmpleadoCol, nombreCol, apellidosCol, emailCol, telefonoCol, activoCol);

        profesoresData = FXCollections.observableArrayList();
        table.setItems(profesoresData);

        return table;
    }

    private void cargarTodosLosDatos() {
        cargarPadres();
        cargarAlumnos();
        cargarProfesores();
    }

    private void cargarPadres() {
        try {
            List<Usuario> padres = UsuarioController.obtenerUsuariosPorRol(Rol.PADRE_FAMILIA);
            usuariosData.clear();
            usuariosData.addAll(padres);
        } catch (Exception e) {
            mostrarError("Error al cargar padres de familia: " + e.getMessage());
        }
    }

    private void cargarAlumnos() {
        try {
            List<Alumno> alumnos = AlumnoController.obtenerAlumnos();
            alumnosData.clear();
            alumnosData.addAll(alumnos);
        } catch (Exception e) {
            mostrarError("Error al cargar alumnos: " + e.getMessage());
        }
    }

    private void cargarProfesores() {
        try {
            List<Profesor> profesores = ProfesorController.obtenerProfesores();
            profesoresData.clear();
            profesoresData.addAll(profesores);
        } catch (Exception e) {
            mostrarError("Error al cargar profesores: " + e.getMessage());
        }
    }

    private void editarUsuarioSeleccionado() {
        Usuario usuario = usuariosTable.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarError("Seleccione un padre de familia para editar.");
            return;
        }

        mostrarDialogoEditarUsuario(usuario);
    }

    private void editarAlumnoSeleccionado() {
        Alumno alumno = alumnosTable.getSelectionModel().getSelectedItem();
        if (alumno == null) {
            mostrarError("Seleccione un alumno para editar.");
            return;
        }

        mostrarDialogoEditarAlumno(alumno);
    }

    private void editarProfesorSeleccionado() {
        Profesor profesor = profesoresTable.getSelectionModel().getSelectedItem();
        if (profesor == null) {
            mostrarError("Seleccione un profesor para editar.");
            return;
        }

        mostrarDialogoEditarProfesor(profesor);
    }

    private void mostrarDialogoEditarUsuario(Usuario usuario) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Editar Padre de Familia");
        dialog.setHeaderText("Editar información de: " + usuario.getNombreCompleto());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nombreField = new TextField(usuario.getNombre());
        TextField apellidosField = new TextField(usuario.getApellidos());
        TextField emailField = new TextField(usuario.getEmail());
        TextField telefonoField = new TextField(usuario.getTelefono());

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Apellidos:"), 0, 1);
        grid.add(apellidosField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Teléfono:"), 0, 3);
        grid.add(telefonoField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                usuario.setNombre(nombreField.getText().trim());
                usuario.setApellidos(apellidosField.getText().trim());
                usuario.setEmail(emailField.getText().trim());
                usuario.setTelefono(telefonoField.getText().trim());

                boolean exito = UsuarioController.actualizarUsuario(usuario);
                if (exito) {
                    mostrarInfo("Usuario actualizado exitosamente.");
                    return true;
                } else {
                    mostrarError("Error al actualizar el usuario.");
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                cargarPadres();
            }
        });
    }

    private void mostrarDialogoEditarAlumno(Alumno alumno) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Editar Alumno");
        dialog.setHeaderText("Editar información de: " + alumno.getNombreCompleto());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField matriculaField = new TextField(alumno.getMatricula());
        TextField nombreField = new TextField(alumno.getNombre());
        TextField apellidosField = new TextField(alumno.getApellidos());
        TextField emailField = new TextField(alumno.getEmail());
        TextField telefonoField = new TextField(alumno.getTelefono());
        TextField direccionField = new TextField(alumno.getDireccion());

        grid.add(new Label("Matrícula:"), 0, 0);
        grid.add(matriculaField, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(nombreField, 1, 1);
        grid.add(new Label("Apellidos:"), 0, 2);
        grid.add(apellidosField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Teléfono:"), 0, 4);
        grid.add(telefonoField, 1, 4);
        grid.add(new Label("Dirección:"), 0, 5);
        grid.add(direccionField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                alumno.setMatricula(matriculaField.getText().trim());
                alumno.setNombre(nombreField.getText().trim());
                alumno.setApellidos(apellidosField.getText().trim());
                alumno.setEmail(emailField.getText().trim());
                alumno.setTelefono(telefonoField.getText().trim());
                alumno.setDireccion(direccionField.getText().trim());

                boolean exito = AlumnoController.actualizarAlumno(alumno);
                if (exito) {
                    mostrarInfo("Alumno actualizado exitosamente.");
                    return true;
                } else {
                    mostrarError("Error al actualizar el alumno.");
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                cargarAlumnos();
            }
        });
    }

    private void mostrarDialogoEditarProfesor(Profesor profesor) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Editar Profesor");
        dialog.setHeaderText("Editar información de: " + profesor.getNombreCompleto());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField numeroEmpleadoField = new TextField(profesor.getNumeroEmpleado());
        TextField nombreField = new TextField(profesor.getNombre());
        TextField apellidosField = new TextField(profesor.getApellidos());
        TextField emailField = new TextField(profesor.getEmail());
        TextField telefonoField = new TextField(profesor.getTelefono());

        grid.add(new Label("No. Empleado:"), 0, 0);
        grid.add(numeroEmpleadoField, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(nombreField, 1, 1);
        grid.add(new Label("Apellidos:"), 0, 2);
        grid.add(apellidosField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Teléfono:"), 0, 4);
        grid.add(telefonoField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                profesor.setNumeroEmpleado(numeroEmpleadoField.getText().trim());
                profesor.setNombre(nombreField.getText().trim());
                profesor.setApellidos(apellidosField.getText().trim());
                profesor.setEmail(emailField.getText().trim());
                profesor.setTelefono(telefonoField.getText().trim());

                boolean exito = ProfesorController.actualizarProfesor(profesor);
                if (exito) {
                    mostrarInfo("Profesor actualizado exitosamente.");
                    return true;
                } else {
                    mostrarError("Error al actualizar el profesor.");
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                cargarProfesores();
            }
        });
    }

    private void toggleActivacionUsuario() {
        Usuario usuario = usuariosTable.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarError("Seleccione un usuario.");
            return;
        }

        boolean nuevoEstado = !usuario.isActivo();
        usuario.setActivo(nuevoEstado);
        
        boolean exito = UsuarioController.actualizarUsuario(usuario);
        if (exito) {
            mostrarInfo("Estado del usuario actualizado exitosamente.");
            cargarPadres();
        } else {
            mostrarError("Error al actualizar el estado del usuario.");
        }
    }

    private void toggleActivacionAlumno() {
        Alumno alumno = alumnosTable.getSelectionModel().getSelectedItem();
        if (alumno == null) {
            mostrarError("Seleccione un alumno.");
            return;
        }

        boolean nuevoEstado = !alumno.isActivo();
        alumno.setActivo(nuevoEstado);
        
        boolean exito = AlumnoController.actualizarAlumno(alumno);
        if (exito) {
            mostrarInfo("Estado del alumno actualizado exitosamente.");
            cargarAlumnos();
        } else {
            mostrarError("Error al actualizar el estado del alumno.");
        }
    }

    private void toggleActivacionProfesor() {
        Profesor profesor = profesoresTable.getSelectionModel().getSelectedItem();
        if (profesor == null) {
            mostrarError("Seleccione un profesor.");
            return;
        }

        boolean nuevoEstado = !profesor.isActivo();
        profesor.setActivo(nuevoEstado);
        
        boolean exito = ProfesorController.actualizarProfesor(profesor);
        if (exito) {
            mostrarInfo("Estado del profesor actualizado exitosamente.");
            cargarProfesores();
        } else {
            mostrarError("Error al actualizar el estado del profesor.");
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

    public void show() {
        primaryStage.show();
    }
}