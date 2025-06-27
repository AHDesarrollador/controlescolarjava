package com.controlescolar.views;

import com.controlescolar.controllers.PadreAlumnoController;
import com.controlescolar.controllers.UsuarioController;
import com.controlescolar.controllers.AlumnoController;
import com.controlescolar.controllers.AuthController;
import com.controlescolar.models.Usuario;
import com.controlescolar.models.Alumno;
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

public class GestionPadresView {
    private Stage primaryStage;
    private TableView<VinculacionData> vinculacionesTable;
    private ObservableList<VinculacionData> vinculacionesData;

    public GestionPadresView(Stage parentStage) {
        this.primaryStage = new Stage();
        this.primaryStage.initModality(Modality.WINDOW_MODAL);
        this.primaryStage.initOwner(parentStage);
        initializeView();
    }

    private void initializeView() {
        // Verificar permisos
        if (!AuthController.canManageParentStudentRelations()) {
            mostrarError("No tiene permisos para gestionar vinculaciones padre-alumno.");
            primaryStage.close();
            return;
        }

        primaryStage.setTitle("Gestión de Padres de Familia");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));

        // Header
        HBox header = createHeader();

        // Botones de acción
        HBox buttonBar = createButtonBar();

        // Tabla de vinculaciones
        vinculacionesTable = createVinculacionesTable();

        mainLayout.getChildren().addAll(header, buttonBar, vinculacionesTable);

        Scene scene = new Scene(mainLayout);
        primaryStage.setScene(scene);

        cargarVinculaciones();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Gestión de Padres de Familia");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.DARKBLUE);

        header.getChildren().add(titleLabel);
        return header;
    }

    private HBox createButtonBar() {
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        Button vincularBtn = new Button("Vincular Padre-Alumno");
        vincularBtn.setOnAction(e -> mostrarDialogoVincular());

        Button desvincularBtn = new Button("Desvincular");
        desvincularBtn.setOnAction(e -> desvincularSeleccionado());

        Button actualizarBtn = new Button("Actualizar");
        actualizarBtn.setOnAction(e -> cargarVinculaciones());

        buttonBar.getChildren().addAll(vincularBtn, desvincularBtn, actualizarBtn);
        return buttonBar;
    }

    private TableView<VinculacionData> createVinculacionesTable() {
        TableView<VinculacionData> table = new TableView<>();
        table.setPrefHeight(500);

        TableColumn<VinculacionData, String> padreCol = new TableColumn<>("Padre");
        padreCol.setCellValueFactory(new PropertyValueFactory<>("nombrePadre"));
        padreCol.setPrefWidth(200);

        TableColumn<VinculacionData, String> alumnoCol = new TableColumn<>("Alumno");
        alumnoCol.setCellValueFactory(new PropertyValueFactory<>("nombreAlumno"));
        alumnoCol.setPrefWidth(200);

        TableColumn<VinculacionData, String> parentescoCol = new TableColumn<>("Parentesco");
        parentescoCol.setCellValueFactory(new PropertyValueFactory<>("parentesco"));
        parentescoCol.setPrefWidth(150);

        TableColumn<VinculacionData, String> autorizadoCol = new TableColumn<>("Autorizado");
        autorizadoCol.setCellValueFactory(new PropertyValueFactory<>("autorizado"));
        autorizadoCol.setPrefWidth(100);

        TableColumn<VinculacionData, String> fechaCol = new TableColumn<>("Fecha Vinculación");
        fechaCol.setCellValueFactory(new PropertyValueFactory<>("fechaVinculacion"));
        fechaCol.setPrefWidth(150);

        table.getColumns().addAll(padreCol, alumnoCol, parentescoCol, autorizadoCol, fechaCol);

        vinculacionesData = FXCollections.observableArrayList();
        table.setItems(vinculacionesData);

        return table;
    }

    private void cargarVinculaciones() {
        vinculacionesData.clear();
        try {
            // Obtener todos los padres de familia
            List<Usuario> padres = UsuarioController.obtenerUsuariosPorRol(Rol.PADRE_FAMILIA);
            
            for (Usuario padre : padres) {
                List<Alumno> alumnosVinculados = PadreAlumnoController.obtenerAlumnosPorPadre(padre.getId());
                
                if (alumnosVinculados.isEmpty()) {
                    // Mostrar padre sin alumnos vinculados
                    vinculacionesData.add(new VinculacionData(
                        padre.getId(),
                        null,
                        padre.getNombreCompleto(),
                        "Sin alumnos vinculados",
                        "",
                        "No",
                        ""
                    ));
                } else {
                    // Mostrar vinculaciones
                    for (Alumno alumno : alumnosVinculados) {
                        vinculacionesData.add(new VinculacionData(
                            padre.getId(),
                            alumno.getId(),
                            padre.getNombreCompleto(),
                            alumno.getNombreCompleto(),
                            "Padre/Madre", // Podrías obtener esto de la vinculación real
                            "Sí",
                            "Fecha actual" // Podrías obtener esto de la vinculación real
                        ));
                    }
                }
            }
        } catch (Exception e) {
            mostrarError("Error al cargar vinculaciones: " + e.getMessage());
        }
    }

    private void mostrarDialogoVincular() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Vincular Padre-Alumno");
        dialog.setHeaderText("Seleccione el padre y el alumno a vincular");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // ComboBox para padres
        ComboBox<Usuario> padresCombo = new ComboBox<>();
        try {
            List<Usuario> padres = UsuarioController.obtenerUsuariosPorRol(Rol.PADRE_FAMILIA);
            padresCombo.setItems(FXCollections.observableArrayList(padres));
        } catch (Exception e) {
            mostrarError("Error al cargar padres: " + e.getMessage());
        }

        // ComboBox para alumnos
        ComboBox<Alumno> alumnosCombo = new ComboBox<>();
        try {
            List<Alumno> alumnos = AlumnoController.obtenerAlumnos();
            alumnosCombo.setItems(FXCollections.observableArrayList(alumnos));
        } catch (Exception e) {
            mostrarError("Error al cargar alumnos: " + e.getMessage());
        }

        // Campo de parentesco
        TextField parentescoField = new TextField();
        parentescoField.setPromptText("Ej: Padre, Madre, Tutor");

        grid.add(new Label("Padre:"), 0, 0);
        grid.add(padresCombo, 1, 0);
        grid.add(new Label("Alumno:"), 0, 1);
        grid.add(alumnosCombo, 1, 1);
        grid.add(new Label("Parentesco:"), 0, 2);
        grid.add(parentescoField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType vincularButtonType = new ButtonType("Vincular", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(vincularButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == vincularButtonType) {
                Usuario padreSeleccionado = padresCombo.getSelectionModel().getSelectedItem();
                Alumno alumnoSeleccionado = alumnosCombo.getSelectionModel().getSelectedItem();
                String parentesco = parentescoField.getText().trim();

                if (padreSeleccionado == null || alumnoSeleccionado == null || parentesco.isEmpty()) {
                    mostrarError("Complete todos los campos.");
                    return false;
                }

                boolean exito = PadreAlumnoController.vincularPadreAlumno(
                    padreSeleccionado.getId(),
                    alumnoSeleccionado.getId(),
                    parentesco
                );

                if (exito) {
                    mostrarInfo("Vinculación creada exitosamente.");
                    return true;
                } else {
                    mostrarError("Error al crear la vinculación.");
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                cargarVinculaciones();
            }
        });
    }

    private void desvincularSeleccionado() {
        VinculacionData seleccionada = vinculacionesTable.getSelectionModel().getSelectedItem();
        if (seleccionada == null || seleccionada.getAlumnoId() == null) {
            mostrarError("Seleccione una vinculación válida.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Desvinculación");
        confirmacion.setHeaderText("¿Está seguro de desvincular esta relación?");
        confirmacion.setContentText("Padre: " + seleccionada.getNombrePadre() + 
                                   "\nAlumno: " + seleccionada.getNombreAlumno());

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean exito = PadreAlumnoController.desvincularPadreAlumno(
                    seleccionada.getPadreId(),
                    seleccionada.getAlumnoId()
                );

                if (exito) {
                    mostrarInfo("Desvinculación exitosa.");
                    cargarVinculaciones();
                } else {
                    mostrarError("Error al desvincular.");
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

    // Clase para datos de la tabla
    public static class VinculacionData {
        private org.bson.types.ObjectId padreId;
        private org.bson.types.ObjectId alumnoId;
        private String nombrePadre;
        private String nombreAlumno;
        private String parentesco;
        private String autorizado;
        private String fechaVinculacion;

        public VinculacionData(org.bson.types.ObjectId padreId, org.bson.types.ObjectId alumnoId, 
                              String nombrePadre, String nombreAlumno, String parentesco, 
                              String autorizado, String fechaVinculacion) {
            this.padreId = padreId;
            this.alumnoId = alumnoId;
            this.nombrePadre = nombrePadre;
            this.nombreAlumno = nombreAlumno;
            this.parentesco = parentesco;
            this.autorizado = autorizado;
            this.fechaVinculacion = fechaVinculacion;
        }

        // Getters
        public org.bson.types.ObjectId getPadreId() { return padreId; }
        public org.bson.types.ObjectId getAlumnoId() { return alumnoId; }
        public String getNombrePadre() { return nombrePadre; }
        public String getNombreAlumno() { return nombreAlumno; }
        public String getParentesco() { return parentesco; }
        public String getAutorizado() { return autorizado; }
        public String getFechaVinculacion() { return fechaVinculacion; }
    }
}