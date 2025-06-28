//GestionUsuariosView.java
package com.controlescolar.views;

import com.controlescolar.controllers.UsuarioController;
import com.controlescolar.models.Usuario;
import com.controlescolar.enums.Rol;
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

import java.util.List;
import java.util.Optional;

public class GestionUsuariosView extends Application {
    private Stage stage;
    private Usuario usuarioActual;
    private TableView<Usuario> tablaUsuarios;
    private ObservableList<Usuario> listaUsuarios;
    private List<Usuario> todosLosUsuarios; // Lista completa sin filtrar
    private TextField buscarField;
    private ComboBox<Rol> filtroRolCombo;
    private ComboBox<String> filtroEstadoCombo;
    
    // Labels para estadísticas
    private Label totalLabel;
    private Label activosLabel;
    private Label inactivosLabel;
    private Label adminsLabel;
    
    // Botones
    private Button btnAgregar;
    private Button btnEditar;
    private Button btnEliminar;
    private Button btnActivar;
    private Button btnCambiarRol;
    private Button btnResetPassword;

    public GestionUsuariosView(Usuario usuario) {
        this.usuarioActual = usuario;
        this.listaUsuarios = FXCollections.observableArrayList();
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Gestión de Usuarios - Sistema Control Escolar");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        root.setTop(createHeader());
        
        // Centro - tabla y controles
        root.setCenter(createCenterContent());
        
        // Footer con botones
        root.setBottom(createFooter());
        
        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.show();
        
        // Cargar datos iniciales
        cargarUsuarios();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #2c3e50;");
        
        Label titulo = new Label("👥 Gestión de Usuarios");
        titulo.setTextFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Barra de herramientas
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
        buscarField.setPromptText("Buscar por nombre, email...");
        buscarField.setPrefWidth(200);
        buscarField.textProperty().addListener((obs, oldText, newText) -> filtrarUsuarios());
        
        // Filtro por rol
        Label rolLabel = new Label("Rol:");
        rolLabel.setTextFill(Color.WHITE);
        
        filtroRolCombo = new ComboBox<>();
        filtroRolCombo.getItems().add(null); // "Todos"
        filtroRolCombo.getItems().addAll(Rol.values());
        filtroRolCombo.setConverter(new javafx.util.StringConverter<Rol>() {
            @Override
            public String toString(Rol rol) {
                return rol == null ? "Todos los roles" : rol.toString();
            }
            @Override
            public Rol fromString(String string) {
                return null;
            }
        });
        filtroRolCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrarUsuarios());
        
        // Filtro por estado
        Label estadoLabel = new Label("Estado:");
        estadoLabel.setTextFill(Color.WHITE);
        
        filtroEstadoCombo = new ComboBox<>();
        filtroEstadoCombo.getItems().addAll("Todos", "Activos", "Inactivos");
        filtroEstadoCombo.setValue("Todos");
        filtroEstadoCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrarUsuarios());
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Botón refrescar
        Button btnRefrescar = new Button("🔄 Actualizar");
        btnRefrescar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnRefrescar.setOnAction(e -> cargarUsuarios());
        
        toolBar.getChildren().addAll(buscarLabel, buscarField, rolLabel, filtroRolCombo, 
                estadoLabel, filtroEstadoCombo, spacer, btnRefrescar);
        return toolBar;
    }

    private VBox createCenterContent() {
        VBox centerContent = new VBox(10);
        centerContent.setPadding(new Insets(20));
        
        // Estadísticas
        HBox estadisticas = createEstadisticas();
        
        // Tabla de usuarios
        VBox tablaContainer = createTablaUsuarios();
        
        centerContent.getChildren().addAll(estadisticas, tablaContainer);
        return centerContent;
    }

    private HBox createEstadisticas() {
        HBox estadisticas = new HBox(20);
        estadisticas.setAlignment(Pos.CENTER);
        estadisticas.setPadding(new Insets(10));
        estadisticas.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5px;");
        
        totalLabel = new Label("Total: 0");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        activosLabel = new Label("Activos: 0");
        activosLabel.setTextFill(Color.GREEN);
        activosLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        inactivosLabel = new Label("Inactivos: 0");
        inactivosLabel.setTextFill(Color.RED);
        inactivosLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        adminsLabel = new Label("Administradores: 0");
        adminsLabel.setTextFill(Color.BLUE);
        adminsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        estadisticas.getChildren().addAll(totalLabel, activosLabel, inactivosLabel, adminsLabel);
        return estadisticas;
    }

    private VBox createTablaUsuarios() {
        VBox container = new VBox(10);
        
        tablaUsuarios = new TableView<>();
        tablaUsuarios.setItems(listaUsuarios);
        
        // Columnas
        TableColumn<Usuario, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            String nombreCompleto = usuario.getNombre() + " " + (usuario.getApellidos() != null ? usuario.getApellidos() : "");
            return new javafx.beans.property.SimpleStringProperty(nombreCompleto);
        });
        colNombre.setPrefWidth(200);
        
        TableColumn<Usuario, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(250);
        
        TableColumn<Usuario, String> colRol = new TableColumn<>("Rol");
        colRol.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRol().toString());
        });
        colRol.setPrefWidth(120);
        
        TableColumn<Usuario, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTelefono.setPrefWidth(120);
        
        TableColumn<Usuario, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> {
            String estado = cellData.getValue().isActivo() ? "Activo" : "Inactivo";
            return new javafx.beans.property.SimpleStringProperty(estado);
        });
        colEstado.setCellFactory(column -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    if ("Activo".equals(estado)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
        colEstado.setPrefWidth(100);
        
        TableColumn<Usuario, String> colFechaCreacion = new TableColumn<>("Fecha Creación");
        colFechaCreacion.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaCreacion() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getFechaCreacion().toLocalDate().toString()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colFechaCreacion.setPrefWidth(120);
        
        tablaUsuarios.getColumns().addAll(colNombre, colEmail, colRol, colTelefono, colEstado, colFechaCreacion);
        
        // Listener para selección
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            actualizarEstadoBotones();
        });
        
        container.getChildren().add(tablaUsuarios);
        VBox.setVgrow(tablaUsuarios, Priority.ALWAYS);
        
        return container;
    }

    private HBox createFooter() {
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 1px 0 0 0;");
        
        // Botones principales
        btnAgregar = new Button("➕ Nuevo Usuario");
        btnAgregar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        btnAgregar.setOnAction(e -> mostrarFormularioUsuario(null));
        
        btnEditar = new Button("✏️ Editar");
        btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        btnEditar.setOnAction(e -> editarUsuarioSeleccionado());
        
        btnEliminar = new Button("🗑️ Desactivar");
        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        btnEliminar.setOnAction(e -> desactivarUsuarioSeleccionado());
        
        btnActivar = new Button("✅ Activar");
        btnActivar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        btnActivar.setOnAction(e -> activarUsuarioSeleccionado());
        
        btnCambiarRol = new Button("🔄 Cambiar Rol");
        btnCambiarRol.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        btnCambiarRol.setOnAction(e -> cambiarRolUsuario());
        
        btnResetPassword = new Button("🔑 Reset Password");
        btnResetPassword.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        btnResetPassword.setOnAction(e -> resetearPasswordUsuario());
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Botón cerrar
        Button btnCerrar = new Button("❌ Cerrar");
        btnCerrar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        btnCerrar.setOnAction(e -> stage.close());
        
        footer.getChildren().addAll(btnAgregar, btnEditar, btnEliminar, btnActivar, 
                btnCambiarRol, btnResetPassword, spacer, btnCerrar);
        
        // Inicializar estado de botones
        actualizarEstadoBotones();
        
        return footer;
    }

    private void cargarUsuarios() {
        try {
            todosLosUsuarios = UsuarioController.obtenerUsuarios();
            filtrarUsuarios(); // Aplicar filtros actuales
        } catch (Exception e) {
            mostrarError("Error al cargar usuarios: " + e.getMessage());
        }
    }

    private void filtrarUsuarios() {
        if (todosLosUsuarios == null) {
            return;
        }
        
        String textoBusqueda = buscarField != null ? buscarField.getText().toLowerCase().trim() : "";
        Rol rolSeleccionado = filtroRolCombo != null ? filtroRolCombo.getValue() : null;
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
            .collect(java.util.stream.Collectors.toList());
        
        listaUsuarios.clear();
        listaUsuarios.addAll(usuariosFiltrados);
        actualizarEstadisticas();
    }

    private void actualizarEstadisticas() {
        if (listaUsuarios == null || totalLabel == null) {
            return;
        }
        
        int total = listaUsuarios.size();
        long activos = listaUsuarios.stream().filter(Usuario::isActivo).count();
        long inactivos = total - activos;
        long administradores = listaUsuarios.stream().filter(u -> u.getRol() == Rol.ADMINISTRADOR).count();
        
        totalLabel.setText("Total: " + total);
        activosLabel.setText("Activos: " + activos);
        inactivosLabel.setText("Inactivos: " + inactivos);
        adminsLabel.setText("Administradores: " + administradores);
    }

    private void actualizarEstadoBotones() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        boolean haySeleccion = usuarioSeleccionado != null;
        
        btnEditar.setDisable(!haySeleccion);
        btnEliminar.setDisable(!haySeleccion || (haySeleccion && !usuarioSeleccionado.isActivo()));
        btnActivar.setDisable(!haySeleccion || (haySeleccion && usuarioSeleccionado.isActivo()));
        btnCambiarRol.setDisable(!haySeleccion);
        btnResetPassword.setDisable(!haySeleccion);
    }

    private void mostrarFormularioUsuario(Usuario usuario) {
        // Crear formulario para nuevo usuario o editar existente
        Stage formularioStage = new Stage();
        formularioStage.setTitle(usuario == null ? "Nuevo Usuario" : "Editar Usuario");
        formularioStage.initOwner(stage);
        
        VBox formulario = new VBox(15);
        formulario.setPadding(new Insets(20));
        
        // Campos del formulario
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre");
        if (usuario != null) nombreField.setText(usuario.getNombre());
        
        TextField apellidosField = new TextField();
        apellidosField.setPromptText("Apellidos");
        if (usuario != null && usuario.getApellidos() != null) apellidosField.setText(usuario.getApellidos());
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        if (usuario != null) emailField.setText(usuario.getEmail());
        
        TextField telefonoField = new TextField();
        telefonoField.setPromptText("Teléfono");
        if (usuario != null && usuario.getTelefono() != null) telefonoField.setText(usuario.getTelefono());
        
        ComboBox<Rol> rolCombo = new ComboBox<>();
        rolCombo.getItems().addAll(Rol.values());
        if (usuario != null) rolCombo.setValue(usuario.getRol());
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(usuario == null ? "Contraseña" : "Nueva contraseña (dejar vacío para mantener)");
        
        CheckBox activoCheck = new CheckBox("Usuario activo");
        if (usuario != null) activoCheck.setSelected(usuario.isActivo());
        else activoCheck.setSelected(true);
        
        // Botones
        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER);
        
        Button btnGuardar = new Button(usuario == null ? "Crear Usuario" : "Guardar Cambios");
        btnGuardar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnGuardar.setOnAction(e -> {
            if (guardarUsuario(usuario, nombreField.getText(), apellidosField.getText(), 
                    emailField.getText(), telefonoField.getText(), rolCombo.getValue(), 
                    passwordField.getText(), activoCheck.isSelected())) {
                formularioStage.close();
                cargarUsuarios();
            }
        });
        
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> formularioStage.close());
        
        botones.getChildren().addAll(btnGuardar, btnCancelar);
        
        formulario.getChildren().addAll(
            new Label("Nombre:"), nombreField,
            new Label("Apellidos:"), apellidosField,
            new Label("Email:"), emailField,
            new Label("Teléfono:"), telefonoField,
            new Label("Rol:"), rolCombo,
            new Label("Contraseña:"), passwordField,
            activoCheck,
            botones
        );
        
        Scene scene = new Scene(formulario, 400, 500);
        formularioStage.setScene(scene);
        formularioStage.showAndWait();
    }

    private boolean guardarUsuario(Usuario usuarioExistente, String nombre, String apellidos, 
            String email, String telefono, Rol rol, String password, boolean activo) {
        
        if (nombre.trim().isEmpty() || email.trim().isEmpty() || rol == null) {
            mostrarError("Por favor complete todos los campos obligatorios");
            return false;
        }
        
        if (usuarioExistente == null && password.trim().isEmpty()) {
            mostrarError("La contraseña es obligatoria para usuarios nuevos");
            return false;
        }
        
        try {
            if (usuarioExistente == null) {
                // Crear nuevo usuario
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setNombre(nombre);
                nuevoUsuario.setApellidos(apellidos);
                nuevoUsuario.setEmail(email);
                nuevoUsuario.setTelefono(telefono);
                nuevoUsuario.setRol(rol);
                nuevoUsuario.setPassword(password);
                nuevoUsuario.setActivo(activo);
                
                if (UsuarioController.crearUsuario(nuevoUsuario)) {
                    mostrarInfo("Usuario creado exitosamente");
                    return true;
                } else {
                    mostrarError("Error al crear usuario. El email podría ya existir.");
                    return false;
                }
            } else {
                // Actualizar usuario existente
                usuarioExistente.setNombre(nombre);
                usuarioExistente.setApellidos(apellidos);
                usuarioExistente.setTelefono(telefono);
                usuarioExistente.setActivo(activo);
                
                // Cambiar rol si es diferente
                if (!usuarioExistente.getRol().equals(rol)) {
                    UsuarioController.cambiarRol(usuarioExistente.getId(), rol);
                }
                
                // Cambiar contraseña si se proporcionó una nueva
                if (!password.trim().isEmpty()) {
                    UsuarioController.resetearPassword(usuarioExistente.getId(), password);
                }
                
                if (UsuarioController.actualizarUsuario(usuarioExistente)) {
                    mostrarInfo("Usuario actualizado exitosamente");
                    return true;
                } else {
                    mostrarError("Error al actualizar usuario");
                    return false;
                }
            }
        } catch (Exception e) {
            mostrarError("Error al guardar usuario: " + e.getMessage());
            return false;
        }
    }

    private void editarUsuarioSeleccionado() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado != null) {
            mostrarFormularioUsuario(usuarioSeleccionado);
        }
    }

    private void desactivarUsuarioSeleccionado() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar desactivación");
            confirmacion.setHeaderText("¿Está seguro de desactivar este usuario?");
            confirmacion.setContentText("El usuario " + usuarioSeleccionado.getNombre() + " será desactivado.");
            
            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                if (UsuarioController.eliminarUsuario(usuarioSeleccionado.getId())) {
                    mostrarInfo("Usuario desactivado exitosamente");
                    cargarUsuarios();
                } else {
                    mostrarError("Error al desactivar usuario");
                }
            }
        }
    }

    private void activarUsuarioSeleccionado() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado != null) {
            if (UsuarioController.activarUsuario(usuarioSeleccionado.getId())) {
                mostrarInfo("Usuario activado exitosamente");
                cargarUsuarios();
            } else {
                mostrarError("Error al activar usuario");
            }
        }
    }

    private void cambiarRolUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado != null) {
            ChoiceDialog<Rol> dialog = new ChoiceDialog<>(usuarioSeleccionado.getRol(), Rol.values());
            dialog.setTitle("Cambiar Rol");
            dialog.setHeaderText("Seleccione el nuevo rol para " + usuarioSeleccionado.getNombre());
            dialog.setContentText("Rol:");
            
            Optional<Rol> resultado = dialog.showAndWait();
            if (resultado.isPresent() && !resultado.get().equals(usuarioSeleccionado.getRol())) {
                if (UsuarioController.cambiarRol(usuarioSeleccionado.getId(), resultado.get())) {
                    mostrarInfo("Rol cambiado exitosamente");
                    cargarUsuarios();
                } else {
                    mostrarError("Error al cambiar rol");
                }
            }
        }
    }

    private void resetearPasswordUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Resetear Contraseña");
            dialog.setHeaderText("Resetear contraseña para " + usuarioSeleccionado.getNombre());
            dialog.setContentText("Nueva contraseña:");
            
            Optional<String> resultado = dialog.showAndWait();
            if (resultado.isPresent() && !resultado.get().trim().isEmpty()) {
                if (UsuarioController.resetearPassword(usuarioSeleccionado.getId(), resultado.get())) {
                    mostrarInfo("Contraseña reseteada exitosamente");
                } else {
                    mostrarError("Error al resetear contraseña");
                }
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Para testing independiente
    }

    public static void main(String[] args) {
        launch(args);
    }
}