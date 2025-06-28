package com.controlescolar.views;

import com.controlescolar.controllers.AuthController;
import com.controlescolar.controllers.UsuarioController;
import com.controlescolar.models.Usuario;
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

public class GestionUsuariosDirectorView {
    private Stage primaryStage;
    private TableView<Usuario> usuariosTable;
    private ObservableList<Usuario> usuariosData;
    private List<Usuario> todosLosUsuarios;
    private ComboBox<Rol> filtroRolCombo;
    private TextField buscarField;
    private ComboBox<String> filtroEstadoCombo;

    public GestionUsuariosDirectorView(Stage parentStage) {
        this.primaryStage = new Stage();
        this.primaryStage.initModality(Modality.WINDOW_MODAL);
        this.primaryStage.initOwner(parentStage);
        initializeView();
    }

    private void initializeView() {
        // Verificar permisos
        if (!AuthController.canManageNonAdminUsers()) {
            mostrarError("No tiene permisos para gestionar usuarios.");
            primaryStage.close();
            return;
        }

        primaryStage.setTitle("Gestión de Usuarios - Director");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));

        // Header
        HBox header = createHeader();

        // Filtros y botones
        HBox controlsPanel = createControlsPanel();

        // Tabla de usuarios
        usuariosTable = createUsuariosTable();

        mainLayout.getChildren().addAll(header, controlsPanel, usuariosTable);

        Scene scene = new Scene(mainLayout);
        primaryStage.setScene(scene);

        cargarUsuarios();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Gestión de Usuarios");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.DARKBLUE);

        Label subtitleLabel = new Label("(Excepto Administradores)");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.GRAY);

        header.getChildren().addAll(titleLabel, new Label(" "), subtitleLabel);
        return header;
    }

    private HBox createControlsPanel() {
        HBox controlsPanel = new HBox(15);
        controlsPanel.setAlignment(Pos.CENTER_LEFT);
        controlsPanel.setPadding(new Insets(10));

        // Campo de búsqueda
        Label buscarLabel = new Label("Buscar:");
        buscarField = new TextField();
        buscarField.setPromptText("Nombre, email...");
        buscarField.setPrefWidth(150);
        buscarField.textProperty().addListener((obs, oldText, newText) -> aplicarFiltro());
        
        // Filtro por rol
        Label filtroLabel = new Label("Rol:");
        filtroRolCombo = new ComboBox<>();
        filtroRolCombo.getItems().addAll(
            null, // Para mostrar todos
            Rol.DIRECTOR,
            Rol.SECRETARIO,
            Rol.PROFESOR,
            Rol.ALUMNO,
            Rol.PADRE_FAMILIA
        );
        filtroRolCombo.setPromptText("Todos");
        filtroRolCombo.setOnAction(e -> aplicarFiltro());
        
        // Filtro por estado
        Label estadoLabel = new Label("Estado:");
        filtroEstadoCombo = new ComboBox<>();
        filtroEstadoCombo.getItems().addAll("Todos", "Activos", "Inactivos");
        filtroEstadoCombo.setValue("Todos");
        filtroEstadoCombo.setOnAction(e -> aplicarFiltro());

        // Botones de acción
        Button crearBtn = new Button("Crear Usuario");
        crearBtn.setOnAction(e -> mostrarDialogoCrearUsuario());

        Button editarBtn = new Button("Editar Usuario");
        editarBtn.setOnAction(e -> editarUsuarioSeleccionado());

        Button cambiarRolBtn = new Button("Cambiar Rol");
        cambiarRolBtn.setOnAction(e -> cambiarRolUsuario());

        Button activarBtn = new Button("Activar/Desactivar");
        activarBtn.setOnAction(e -> toggleActivacionUsuario());

        Button resetPasswordBtn = new Button("Resetear Contraseña");
        resetPasswordBtn.setOnAction(e -> resetearPassword());

        Button actualizarBtn = new Button("Actualizar");
        actualizarBtn.setOnAction(e -> cargarUsuarios());

        controlsPanel.getChildren().addAll(
            buscarLabel, buscarField,
            filtroLabel, filtroRolCombo,
            estadoLabel, filtroEstadoCombo,
            new Separator(),
            crearBtn, editarBtn, cambiarRolBtn, activarBtn, resetPasswordBtn, actualizarBtn
        );

        return controlsPanel;
    }

    private TableView<Usuario> createUsuariosTable() {
        TableView<Usuario> table = new TableView<>();
        table.setPrefHeight(500);
        
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
        nombreCol.setPrefWidth(120);

        TableColumn<Usuario, String> apellidosCol = new TableColumn<>("Apellidos");
        apellidosCol.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        apellidosCol.setPrefWidth(120);

        TableColumn<Usuario, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<Usuario, String> rolCol = new TableColumn<>("Rol");
        rolCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getRol().getNombre()
            )
        );
        rolCol.setPrefWidth(150);

        TableColumn<Usuario, String> telefonoCol = new TableColumn<>("Teléfono");
        telefonoCol.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        telefonoCol.setPrefWidth(120);

        TableColumn<Usuario, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isActivo() ? "Activo" : "Inactivo"
            )
        );
        estadoCol.setPrefWidth(80);

        TableColumn<Usuario, String> fechaCreacionCol = new TableColumn<>("Fecha Creación");
        fechaCreacionCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFechaCreacion() != null ? 
                cellData.getValue().getFechaCreacion().toLocalDate().toString() : ""
            )
        );
        fechaCreacionCol.setPrefWidth(120);

        table.getColumns().addAll(nombreCol, apellidosCol, emailCol, rolCol, telefonoCol, estadoCol, fechaCreacionCol);

        usuariosData = FXCollections.observableArrayList();
        table.setItems(usuariosData);

        return table;
    }

    private void cargarUsuarios() {
        try {
            // Cargar todos los usuarios excepto administradores
            todosLosUsuarios = UsuarioController.obtenerUsuarios().stream()
                .filter(usuario -> usuario.getRol() != Rol.ADMINISTRADOR)
                .collect(Collectors.toList());
            
            aplicarFiltro(); // Aplicar filtros actuales
            
        } catch (Exception e) {
            mostrarError("Error al cargar usuarios: " + e.getMessage());
        }
    }

    private void aplicarFiltro() {
        if (todosLosUsuarios == null) {
            return;
        }
        
        String textoBusqueda = buscarField != null ? buscarField.getText().toLowerCase().trim() : "";
        Rol rolSeleccionado = filtroRolCombo != null ? filtroRolCombo.getSelectionModel().getSelectedItem() : null;
        String estadoSeleccionado = filtroEstadoCombo != null ? filtroEstadoCombo.getValue() : "Todos";
        
        List<Usuario> usuariosFiltrados = todosLosUsuarios.stream()
            .filter(usuario -> {
                // Filtro por texto (nombre, apellidos, email)
                boolean coincideTexto = textoBusqueda.isEmpty() || 
                    (usuario.getNombre() != null && usuario.getNombre().toLowerCase().contains(textoBusqueda)) ||
                    (usuario.getApellidos() != null && usuario.getApellidos().toLowerCase().contains(textoBusqueda)) ||
                    (usuario.getEmail() != null && usuario.getEmail().toLowerCase().contains(textoBusqueda));
                
                // Filtro por rol
                boolean coincideRol = rolSeleccionado == null || usuario.getRol() == rolSeleccionado;
                
                // Filtro por estado
                boolean coincideEstado = "Todos".equals(estadoSeleccionado) ||
                    ("Activos".equals(estadoSeleccionado) && usuario.isActivo()) ||
                    ("Inactivos".equals(estadoSeleccionado) && !usuario.isActivo());
                
                return coincideTexto && coincideRol && coincideEstado;
            })
            .collect(Collectors.toList());
        
        usuariosData.clear();
        usuariosData.addAll(usuariosFiltrados);
    }

    private void mostrarDialogoCrearUsuario() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Crear Usuario");
        dialog.setHeaderText("Crear nuevo usuario en el sistema");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nombreField = new TextField();
        TextField apellidosField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField telefonoField = new TextField();
        
        ComboBox<Rol> rolCombo = new ComboBox<>();
        rolCombo.getItems().addAll(
            Rol.DIRECTOR,
            Rol.SECRETARIO,
            Rol.PROFESOR,
            Rol.ALUMNO,
            Rol.PADRE_FAMILIA
        );

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Apellidos:"), 0, 1);
        grid.add(apellidosField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Contraseña:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Teléfono:"), 0, 4);
        grid.add(telefonoField, 1, 4);
        grid.add(new Label("Rol:"), 0, 5);
        grid.add(rolCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        ButtonType crearButtonType = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(crearButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == crearButtonType) {
                try {
                    String nombre = nombreField.getText().trim();
                    String apellidos = apellidosField.getText().trim();
                    String email = emailField.getText().trim();
                    String password = passwordField.getText();
                    String telefono = telefonoField.getText().trim();
                    Rol rol = rolCombo.getSelectionModel().getSelectedItem();

                    if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || 
                        password.isEmpty() || rol == null) {
                        mostrarError("Complete todos los campos obligatorios.");
                        return false;
                    }

                    Usuario nuevoUsuario = new Usuario(email, password, rol, nombre, apellidos);
                    nuevoUsuario.setTelefono(telefono);

                    boolean exito = UsuarioController.crearUsuario(nuevoUsuario);
                    if (exito) {
                        mostrarInfo("Usuario creado exitosamente.");
                        return true;
                    } else {
                        mostrarError("Error al crear el usuario. El email podría ya existir.");
                        return false;
                    }

                } catch (Exception e) {
                    mostrarError("Error al crear usuario: " + e.getMessage());
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                cargarUsuarios();
            }
        });
    }

    private void editarUsuarioSeleccionado() {
        Usuario usuario = usuariosTable.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarError("Seleccione un usuario para editar.");
            return;
        }

        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            mostrarError("No puede editar usuarios administradores.");
            return;
        }

        mostrarDialogoEditarUsuario(usuario);
    }

    private void mostrarDialogoEditarUsuario(Usuario usuario) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Editar Usuario");
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
                cargarUsuarios();
            }
        });
    }

    private void cambiarRolUsuario() {
        Usuario usuario = usuariosTable.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarError("Seleccione un usuario.");
            return;
        }

        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            mostrarError("No puede cambiar el rol de un administrador.");
            return;
        }

        Dialog<Rol> dialog = new Dialog<>();
        dialog.setTitle("Cambiar Rol");
        dialog.setHeaderText("Cambiar rol de: " + usuario.getNombreCompleto());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label currentRoleLabel = new Label("Rol actual: " + usuario.getRol().getNombre());
        currentRoleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        ComboBox<Rol> rolCombo = new ComboBox<>();
        rolCombo.getItems().addAll(
            Rol.DIRECTOR,
            Rol.SECRETARIO,
            Rol.PROFESOR,
            Rol.ALUMNO,
            Rol.PADRE_FAMILIA
        );
        rolCombo.setValue(usuario.getRol());

        content.getChildren().addAll(currentRoleLabel, new Label("Nuevo rol:"), rolCombo);
        dialog.getDialogPane().setContent(content);

        ButtonType cambiarButtonType = new ButtonType("Cambiar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(cambiarButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == cambiarButtonType) {
                return rolCombo.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(nuevoRol -> {
            if (nuevoRol != null && nuevoRol != usuario.getRol()) {
                boolean exito = UsuarioController.cambiarRol(usuario.getId(), nuevoRol);
                if (exito) {
                    mostrarInfo("Rol cambiado exitosamente.");
                    cargarUsuarios();
                } else {
                    mostrarError("Error al cambiar el rol.");
                }
            }
        });
    }

    private void toggleActivacionUsuario() {
        Usuario usuario = usuariosTable.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarError("Seleccione un usuario.");
            return;
        }

        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            mostrarError("No puede cambiar el estado de un administrador.");
            return;
        }

        boolean nuevoEstado = !usuario.isActivo();
        String accion = nuevoEstado ? "activar" : "desactivar";
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar acción");
        confirmacion.setHeaderText("¿Está seguro de " + accion + " este usuario?");
        confirmacion.setContentText("Usuario: " + usuario.getNombreCompleto());

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean exito;
                if (nuevoEstado) {
                    exito = UsuarioController.activarUsuario(usuario.getId());
                } else {
                    exito = UsuarioController.eliminarUsuario(usuario.getId());
                }

                if (exito) {
                    mostrarInfo("Estado del usuario actualizado exitosamente.");
                    cargarUsuarios();
                } else {
                    mostrarError("Error al actualizar el estado del usuario.");
                }
            }
        });
    }

    private void resetearPassword() {
        Usuario usuario = usuariosTable.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarError("Seleccione un usuario.");
            return;
        }

        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            mostrarError("No puede resetear la contraseña de un administrador.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Resetear Contraseña");
        dialog.setHeaderText("Resetear contraseña de: " + usuario.getNombreCompleto());
        dialog.setContentText("Nueva contraseña:");

        dialog.showAndWait().ifPresent(nuevaPassword -> {
            if (!nuevaPassword.trim().isEmpty()) {
                boolean exito = UsuarioController.resetearPassword(usuario.getId(), nuevaPassword);
                if (exito) {
                    mostrarInfo("Contraseña reseteada exitosamente.");
                } else {
                    mostrarError("Error al resetear la contraseña.");
                }
            }
        });
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