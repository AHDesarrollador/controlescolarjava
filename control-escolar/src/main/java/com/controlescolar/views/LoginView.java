//LoginView.Java
package com.controlescolar.views;

import com.controlescolar.controllers.AuthController;
import com.controlescolar.models.Usuario;
import com.controlescolar.enums.Rol;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginView extends Application {

    private AuthController authController;
    private TextField usuarioField;
    private PasswordField passwordField;
    private Label mensajeLabel;
    private Button loginButton;
    private Stage primaryStage;

    public LoginView() {
        this.authController = new AuthController();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Configuración de la ventana
        primaryStage.setTitle("Sistema Control Escolar - Login");
        primaryStage.setResizable(false);

        // Layout principal
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(40));
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");

        // Panel de login
        VBox loginPanel = createLoginPanel();

        mainLayout.getChildren().add(loginPanel);

        Scene scene = new Scene(mainLayout, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        // Focus inicial en el campo usuario
        usuarioField.requestFocus();
    }

    private VBox createLoginPanel() {
        VBox panel = new VBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(30));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        panel.setPrefWidth(300);

        // Título
        Label titulo = new Label("Iniciar Sesión");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titulo.setTextFill(Color.valueOf("#333333"));

        // Campos de entrada
        VBox camposBox = createCamposLogin();

        // Botón de login
        loginButton = new Button("Ingresar");
        loginButton.setPrefWidth(250);
        loginButton.setPrefHeight(40);
        loginButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5;");
        loginButton.setOnAction(e -> handleLogin());

        // Mensaje de estado
        mensajeLabel = new Label("");
        mensajeLabel.setWrapText(true);
        mensajeLabel.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(titulo, camposBox, loginButton, mensajeLabel);

        return panel;
    }

    private VBox createCamposLogin() {
        VBox campos = new VBox(10);
        campos.setAlignment(Pos.CENTER);

        // Campo usuario
        Label usuarioLabel = new Label("Usuario:");
        usuarioLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        usuarioField = new TextField();
        usuarioField.setPromptText("Ingresa tu usuario");
        usuarioField.setPrefWidth(250);
        usuarioField.setPrefHeight(35);
        usuarioField.setOnAction(e -> passwordField.requestFocus());

        // Campo contraseña
        Label passwordLabel = new Label("Contraseña:");
        passwordLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        passwordField = new PasswordField();
        passwordField.setPromptText("Ingresa tu contraseña");
        passwordField.setPrefWidth(250);
        passwordField.setPrefHeight(35);
        passwordField.setOnAction(e -> handleLogin());

        campos.getChildren().addAll(usuarioLabel, usuarioField, passwordLabel, passwordField);

        return campos;
    }

    private void handleLogin() {
        String usuario = usuarioField.getText().trim();
        String password = passwordField.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Por favor, completa todos los campos", "error");
            return;
        }

        // Deshabilitar botón durante la validación
        loginButton.setDisable(true);
        loginButton.setText("Validando...");

        try {
            boolean loginExitoso = AuthController.login(usuario, password);
            Usuario usuarioAutenticado = null;
            if (loginExitoso) {
                usuarioAutenticado = AuthController.getUsuarioActual();
            }

            if (usuarioAutenticado != null) {
                mostrarMensaje("Login exitoso", "success");
                abrirDashboard(usuarioAutenticado);
            } else {
                mostrarMensaje("Usuario o contraseña incorrectos", "error");
            }
        } catch (Exception e) {
            mostrarMensaje("Error de conexión: " + e.getMessage(), "error");
        } finally {
            loginButton.setDisable(false);
            loginButton.setText("Ingresar");
        }
    }

    private void abrirDashboard(Usuario usuario) {
        try {
            // Verificar si es padre de familia para redirigir a su vista específica
            if (usuario.getRol() == Rol.PADRE_FAMILIA) {
                PadresDashboardView padresDashboard = new PadresDashboardView(usuario);
                Stage dashboardStage = new Stage();
                padresDashboard.start(dashboardStage);
            } else {
                DashboardView dashboard = new DashboardView(usuario);
                Stage dashboardStage = new Stage();
                dashboard.start(dashboardStage);
            }

            // Cerrar ventana de login
            primaryStage.close();

        } catch (Exception e) {
            mostrarMensaje("Error al abrir dashboard: " + e.getMessage(), "error");
        }
    }

    private void mostrarMensaje(String mensaje, String tipo) {
        mensajeLabel.setText(mensaje);

        if ("error".equals(tipo)) {
            mensajeLabel.setTextFill(Color.RED);
        } else if ("success".equals(tipo)) {
            mensajeLabel.setTextFill(Color.GREEN);
        } else {
            mensajeLabel.setTextFill(Color.valueOf("#333333"));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}