// ProfileView.java
package com.controlescolar.views;

import com.controlescolar.controllers.AuthController;
import com.controlescolar.controllers.ProfileController;
import com.controlescolar.models.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;

public class ProfileView {
    private Stage stage;
    private Usuario usuario;
    private ImageView fotoImageView;
    private TextField nombreField;
    private TextField apellidosField;
    private TextField emailField;
    private TextField telefonoField;
    private Label rolLabel;

    public ProfileView(Usuario usuario) {
        this.usuario = usuario;
        initialize();
    }

    private void initialize() {
        stage = new Stage();
        stage.setTitle("Mi Perfil - " + usuario.getNombreCompleto());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        VBox mainLayout = createMainLayout();
        Scene scene = new Scene(mainLayout, 500, 600);
        stage.setScene(scene);
    }

    private VBox createMainLayout() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #f5f5f5;");

        // Título
        Label titulo = new Label("Mi Perfil");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titulo.setTextFill(Color.valueOf("#333333"));

        // Sección de foto
        VBox fotoSection = createFotoSection();

        // Sección de información personal
        VBox infoSection = createInfoSection();

        // Botones de acción
        HBox buttonSection = createButtonSection();

        layout.getChildren().addAll(titulo, fotoSection, infoSection, buttonSection);
        return layout;
    }

    private VBox createFotoSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Imagen de perfil
        fotoImageView = new ImageView();
        fotoImageView.setFitWidth(120);
        fotoImageView.setFitHeight(120);
        fotoImageView.setPreserveRatio(true);
        fotoImageView.setStyle("-fx-background-radius: 60; -fx-border-radius: 60; -fx-border-color: #ddd; -fx-border-width: 2;");

        cargarFoto();

        // Botones de foto
        HBox fotoBotones = new HBox(10);
        fotoBotones.setAlignment(Pos.CENTER);

        Button cambiarFotoBtn = new Button("Cambiar Foto");
        cambiarFotoBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");
        cambiarFotoBtn.setOnAction(e -> cambiarFoto());

        Button eliminarFotoBtn = new Button("Eliminar");
        eliminarFotoBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5;");
        eliminarFotoBtn.setOnAction(e -> eliminarFoto());

        fotoBotones.getChildren().addAll(cambiarFotoBtn, eliminarFotoBtn);

        section.getChildren().addAll(fotoImageView, fotoBotones);
        return section;
    }

    private VBox createInfoSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label infoTitle = new Label("Información Personal");
        infoTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Campos de información
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        // Nombre
        grid.add(new Label("Nombre:"), 0, 0);
        nombreField = new TextField(usuario.getNombre());
        nombreField.setPrefWidth(200);
        grid.add(nombreField, 1, 0);

        // Apellidos
        grid.add(new Label("Apellidos:"), 0, 1);
        apellidosField = new TextField(usuario.getApellidos());
        apellidosField.setPrefWidth(200);
        grid.add(apellidosField, 1, 1);

        // Email (solo lectura)
        grid.add(new Label("Email:"), 0, 2);
        emailField = new TextField(usuario.getEmail());
        emailField.setEditable(false);
        emailField.setStyle("-fx-background-color: #f0f0f0;");
        emailField.setPrefWidth(200);
        grid.add(emailField, 1, 2);

        // Teléfono
        grid.add(new Label("Teléfono:"), 0, 3);
        telefonoField = new TextField(usuario.getTelefono() != null ? usuario.getTelefono() : "");
        telefonoField.setPrefWidth(200);
        grid.add(telefonoField, 1, 3);

        // Rol (solo lectura)
        grid.add(new Label("Rol:"), 0, 4);
        rolLabel = new Label(usuario.getRol().getNombre());
        rolLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");
        grid.add(rolLabel, 1, 4);

        section.getChildren().addAll(infoTitle, grid);
        return section;
    }

    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);

        Button guardarBtn = new Button("Guardar Cambios");
        guardarBtn.setPrefWidth(120);
        guardarBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 14;");
        guardarBtn.setOnAction(e -> guardarCambios());

        Button passwordBtn = new Button("Cambiar Contraseña");
        passwordBtn.setPrefWidth(140);
        passwordBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 14;");
        passwordBtn.setOnAction(e -> cambiarPassword());

        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.setPrefWidth(100);
        cancelarBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 14;");
        cancelarBtn.setOnAction(e -> stage.close());

        section.getChildren().addAll(guardarBtn, passwordBtn, cancelarBtn);
        return section;
    }

    private void cargarFoto() {
        try {
            if (usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
                File fotoFile = new File(usuario.getFoto());
                if (fotoFile.exists()) {
                    Image imagen = new Image(fotoFile.toURI().toString());
                    fotoImageView.setImage(imagen);
                } else {
                    cargarFotoDefault();
                }
            } else {
                cargarFotoDefault();
            }
        } catch (Exception e) {
            cargarFotoDefault();
        }
    }

    private void cargarFotoDefault() {
        // Imagen por defecto según el rol
        String defaultImage = "/images/default-user.png";
        try {
            Image imagen = new Image(getClass().getResourceAsStream(defaultImage));
            fotoImageView.setImage(imagen);
        } catch (Exception e) {
            // Si no existe la imagen por defecto, crear una imagen simple
            fotoImageView.setImage(null);
        }
    }

    private void cambiarFoto() {
        String nuevaFoto = ProfileController.subirFoto(usuario, stage);
        if (nuevaFoto != null) {
            cargarFoto();
            mostrarMensaje("Foto actualizada correctamente", Alert.AlertType.INFORMATION);
        }
    }

    private void eliminarFoto() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("¿Eliminar foto de perfil?");
        confirmacion.setContentText("¿Está seguro de que desea eliminar su foto de perfil?");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (ProfileController.eliminarFoto(usuario)) {
                cargarFoto();
                mostrarMensaje("Foto eliminada correctamente", Alert.AlertType.INFORMATION);
            }
        }
    }

    private void guardarCambios() {
        String nombre = nombreField.getText().trim();
        String apellidos = apellidosField.getText().trim();
        String telefono = telefonoField.getText().trim();

        if (nombre.isEmpty() || apellidos.isEmpty()) {
            mostrarMensaje("El nombre y apellidos son obligatorios", Alert.AlertType.ERROR);
            return;
        }

        if (ProfileController.actualizarPerfil(usuario, nombre, apellidos, telefono)) {
            mostrarMensaje("Perfil actualizado correctamente", Alert.AlertType.INFORMATION);
        } else {
            mostrarMensaje("Error al actualizar el perfil", Alert.AlertType.ERROR);
        }
    }

    private void cambiarPassword() {
        PasswordChangeDialog dialog = new PasswordChangeDialog();
        dialog.showAndWait().ifPresent(passwords -> {
            String passwordActual = passwords[0];
            String passwordNueva = passwords[1];

            if (!ProfileController.validarPassword(passwordNueva)) {
                mostrarMensaje(ProfileController.getPasswordRequirements(), Alert.AlertType.ERROR);
                return;
            }

            if (ProfileController.cambiarPassword(usuario, passwordActual, passwordNueva)) {
                mostrarMensaje("Contraseña cambiada correctamente", Alert.AlertType.INFORMATION);
            } else {
                mostrarMensaje("Error: contraseña actual incorrecta", Alert.AlertType.ERROR);
            }
        });
    }

    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(tipo == Alert.AlertType.ERROR ? "Error" : "Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void show() {
        stage.show();
    }

    // Clase interna para el diálogo de cambio de contraseña
    private static class PasswordChangeDialog extends Dialog<String[]> {
        public PasswordChangeDialog() {
            setTitle("Cambiar Contraseña");
            setHeaderText("Por favor, ingrese su contraseña actual y la nueva contraseña");

            ButtonType okButtonType = new ButtonType("Cambiar", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            PasswordField actualField = new PasswordField();
            actualField.setPromptText("Contraseña actual");
            PasswordField nuevaField = new PasswordField();
            nuevaField.setPromptText("Nueva contraseña");
            PasswordField confirmarField = new PasswordField();
            confirmarField.setPromptText("Confirmar contraseña");

            grid.add(new Label("Contraseña actual:"), 0, 0);
            grid.add(actualField, 1, 0);
            grid.add(new Label("Nueva contraseña:"), 0, 1);
            grid.add(nuevaField, 1, 1);
            grid.add(new Label("Confirmar:"), 0, 2);
            grid.add(confirmarField, 1, 2);

            getDialogPane().setContent(grid);

            setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    if (!nuevaField.getText().equals(confirmarField.getText())) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Las contraseñas no coinciden");
                        alert.showAndWait();
                        return null;
                    }
                    return new String[]{actualField.getText(), nuevaField.getText()};
                }
                return null;
            });
        }
    }
}