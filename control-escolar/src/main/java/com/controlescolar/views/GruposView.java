// GruposView.java
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GruposView extends Application {

    private Usuario usuarioActual;
    private Stage primaryStage;

    // Componentes UI
    private TableView<Grupo> tablaGrupos;
    private ObservableList<Grupo> listaGrupos;
    private TextField buscarField;
    private Button btnAgregar, btnEditar, btnEliminar, btnRefrescar, btnAsignarAlumnos, btnAsignarMaterias;

    // Formulario
    private TextField codigoField, nombreField, gradoField, seccionField;
    private ComboBox<Profesor> profesorTitularCombo;
    private ComboBox<String> estatusCombo;

    public GruposView(Usuario usuario) {
        this.usuarioActual = usuario;
        this.listaGrupos = FXCollections.observableArrayList();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Control Escolar - Gestión de Grupos");

        VBox root = new VBox();
        root.getChildren().addAll(
                createHeader(),
                createToolBar(),
                createContent()
        );

        Scene scene = new Scene(root, 1200, 700);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        } catch (Exception e) {
            // Si no se puede cargar el CSS, continuar sin estilos
            System.out.println("No se pudo cargar el archivo CSS: " + e.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.show();

        // Cargar datos iniciales
        cargarGrupos();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #34495e;");

        Label titleLabel = new Label("Gestión de Grupos");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("Administra los grupos académicos");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        subtitleLabel.setTextFill(Color.LIGHTGRAY);

        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }

    private HBox createToolBar() {
        HBox toolBar = new HBox(10);
        toolBar.setPadding(new Insets(15));
        toolBar.setStyle("-fx-background-color: #ecf0f1;");

        // Búsqueda
        Label buscarLabel = new Label("Buscar:");
        buscarLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        buscarField = new TextField();
        buscarField.setPromptText("Buscar grupos...");
        buscarField.setPrefWidth(200);
        buscarField.textProperty().addListener((obs, oldText, newText) -> filtrarGrupos(newText));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botones
        btnAgregar = new Button("➕ Agregar Grupo");
        btnAgregar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAgregar.setOnAction(e -> mostrarFormularioAgregar());

        btnEditar = new Button("✏️ Editar");
        btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnEditar.setOnAction(e -> mostrarFormularioEditar());
        btnEditar.setDisable(true);

        btnEliminar = new Button("🗑️ Eliminar");
        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnEliminar.setOnAction(e -> eliminarGrupo());
        btnEliminar.setDisable(true);

        btnAsignarAlumnos = new Button("👥 Asignar Alumnos");
        btnAsignarAlumnos.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAsignarAlumnos.setOnAction(e -> mostrarAsignacionAlumnos());
        btnAsignarAlumnos.setDisable(true);

        btnAsignarMaterias = new Button("📚 Asignar Materias");
        btnAsignarMaterias.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAsignarMaterias.setOnAction(e -> mostrarAsignacionMaterias());
        btnAsignarMaterias.setDisable(true);

        btnRefrescar = new Button("🔄 Refrescar");
        btnRefrescar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        btnRefrescar.setOnAction(e -> cargarGrupos());

        toolBar.getChildren().addAll(buscarLabel, buscarField, spacer, btnAgregar, btnEditar, 
                                     btnEliminar, btnAsignarAlumnos, btnAsignarMaterias, btnRefrescar);
        return toolBar;
    }

    private VBox createContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Tabla
        tablaGrupos = createTablaGrupos();
        content.getChildren().add(tablaGrupos);
        return content;
    }

    private TableView<Grupo> createTablaGrupos() {
        TableView<Grupo> tabla = new TableView<>();
        tabla.setItems(listaGrupos);
        
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

        TableColumn<Grupo, String> colProfesorTitular = new TableColumn<>("Profesor Titular");
        colProfesorTitular.setCellValueFactory(cellData -> {
            ObjectId profesorId = cellData.getValue().getProfesorTitularId();
            if (profesorId != null) {
                Profesor profesor = ProfesorController.obtenerProfesorPorId(profesorId);
                if (profesor != null) {
                    return new javafx.beans.property.SimpleStringProperty(profesor.getNombreCompleto());
                }
            }
            return new javafx.beans.property.SimpleStringProperty("Sin asignar");
        });
        colProfesorTitular.setPrefWidth(200);

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

        // Colorear celda de estatus
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
                                  colProfesorTitular, colAlumnos, colMaterias, colEstatus);

        // Listener para selección
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean seleccionValida = newSelection != null;
            btnEditar.setDisable(!seleccionValida);
            btnEliminar.setDisable(!seleccionValida);
            btnAsignarAlumnos.setDisable(!seleccionValida);
            btnAsignarMaterias.setDisable(!seleccionValida);
        });

        // Doble click para editar
        tabla.setRowFactory(tv -> {
            TableRow<Grupo> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    mostrarFormularioEditar();
                }
            });
            return row;
        });

        return tabla;
    }

    private void cargarGrupos() {
        try {
            List<Grupo> grupos = GrupoController.obtenerTodosLosGrupos();
            listaGrupos.clear();
            listaGrupos.addAll(grupos);
        } catch (Exception e) {
            mostrarError("Error al cargar grupos: " + e.getMessage());
        }
    }

    private void filtrarGrupos(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            cargarGrupos();
            return;
        }

        try {
            List<Grupo> gruposFiltrados = GrupoController.buscarGrupos(filtro);
            listaGrupos.clear();
            listaGrupos.addAll(gruposFiltrados);
        } catch (Exception e) {
            mostrarError("Error al filtrar grupos: " + e.getMessage());
        }
    }

    private void mostrarFormularioAgregar() {
        mostrarFormulario("Agregar Grupo", null);
    }

    private void mostrarFormularioEditar() {
        Grupo seleccionado = tablaGrupos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            mostrarFormulario("Editar Grupo", seleccionado);
        }
    }

    private void mostrarFormulario(String titulo, Grupo grupo) {
        Dialog<Grupo> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);

        // Configurar botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        // Crear formulario
        GridPane grid = createFormulario();

        // Si es edición, llenar campos
        if (grupo != null) {
            llenarFormulario(grupo);
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
                return crearGrupoDesdeFormulario(grupo);
            }
            return null;
        });

        Optional<Grupo> resultado = dialog.showAndWait();

        resultado.ifPresent(grupoGuardado -> {
            try {
                if (grupo == null) {
                    // Agregar nuevo
                    GrupoController.crearGrupo(grupoGuardado);
                    mostrarInfo("Grupo agregado exitosamente");
                } else {
                    // Actualizar existente
                    GrupoController.actualizarGrupo(grupoGuardado);
                    mostrarInfo("Grupo actualizado exitosamente");
                }
                cargarGrupos();
            } catch (Exception e) {
                mostrarError("Error al guardar grupo: " + e.getMessage());
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
        codigoField.setPromptText("Ingrese el código del grupo");
        grid.add(codigoField, 1, 0);

        // Nombre
        grid.add(new Label("Nombre:"), 0, 1);
        nombreField = new TextField();
        nombreField.setPromptText("Ingrese el nombre del grupo");
        grid.add(nombreField, 1, 1);

        // Grado
        grid.add(new Label("Grado:"), 0, 2);
        gradoField = new TextField();
        gradoField.setPromptText("Ej: 1°, 2°, 3°");
        grid.add(gradoField, 1, 2);

        // Sección
        grid.add(new Label("Sección:"), 0, 3);
        seccionField = new TextField();
        seccionField.setPromptText("Ej: A, B, C");
        grid.add(seccionField, 1, 3);

        // Profesor Titular
        grid.add(new Label("Profesor Titular:"), 0, 4);
        profesorTitularCombo = new ComboBox<>();
        profesorTitularCombo.setPromptText("Seleccione un profesor");
        profesorTitularCombo.setPrefWidth(200);
        
        // Configurar cómo se muestra el texto del profesor
        profesorTitularCombo.setConverter(new javafx.util.StringConverter<Profesor>() {
            @Override
            public String toString(Profesor profesor) {
                if (profesor == null) {
                    return null;
                } else {
                    return profesor.getNombreCompleto();
                }
            }

            @Override
            public Profesor fromString(String string) {
                // No necesitamos convertir de string a profesor
                return null;
            }
        });
        
        cargarProfesores();
        grid.add(profesorTitularCombo, 1, 4);

        // Estatus
        grid.add(new Label("Estatus:"), 0, 5);
        estatusCombo = new ComboBox<>();
        estatusCombo.getItems().addAll("Activo", "Inactivo");
        estatusCombo.setValue("Activo");
        grid.add(estatusCombo, 1, 5);

        return grid;
    }

    private void cargarProfesores() {
        try {
            List<Profesor> profesores = ProfesorController.obtenerProfesores();
            profesorTitularCombo.getItems().clear();
            profesorTitularCombo.getItems().addAll(profesores);
        } catch (Exception e) {
            mostrarError("Error al cargar profesores: " + e.getMessage());
        }
    }

    private void llenarFormulario(Grupo grupo) {
        if (codigoField != null) codigoField.setText(grupo.getCodigo() != null ? grupo.getCodigo() : "");
        if (nombreField != null) nombreField.setText(grupo.getNombre() != null ? grupo.getNombre() : "");
        if (gradoField != null) gradoField.setText(grupo.getGrado() != null ? grupo.getGrado() : "");
        if (seccionField != null) seccionField.setText(grupo.getSeccion() != null ? grupo.getSeccion() : "");
        
        if (profesorTitularCombo != null && grupo.getProfesorTitularId() != null) {
            Profesor profesor = ProfesorController.obtenerProfesorPorId(grupo.getProfesorTitularId());
            if (profesor != null) {
                profesorTitularCombo.setValue(profesor);
            }
        }
        
        if (estatusCombo != null) estatusCombo.setValue(grupo.isActivo() ? "Activo" : "Inactivo");
    }

    private void agregarValidacionFormulario(Node guardarButton) {
        // Listener para validar campos requeridos
        Runnable validar = () -> {
            boolean valido = codigoField != null && !codigoField.getText().trim().isEmpty() &&
                    nombreField != null && !nombreField.getText().trim().isEmpty() &&
                    gradoField != null && !gradoField.getText().trim().isEmpty() &&
                    seccionField != null && !seccionField.getText().trim().isEmpty() &&
                    estatusCombo != null && estatusCombo.getValue() != null;
            guardarButton.setDisable(!valido);
        };

        if (codigoField != null) codigoField.textProperty().addListener((obs, oldVal, newVal) -> validar.run());
        if (nombreField != null) nombreField.textProperty().addListener((obs, oldVal, newVal) -> validar.run());
        if (gradoField != null) gradoField.textProperty().addListener((obs, oldVal, newVal) -> validar.run());
        if (seccionField != null) seccionField.textProperty().addListener((obs, oldVal, newVal) -> validar.run());
        if (estatusCombo != null) estatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> validar.run());
    }

    private Grupo crearGrupoDesdeFormulario(Grupo grupoExistente) {
        Grupo grupo = grupoExistente != null ? grupoExistente : new Grupo();

        grupo.setCodigo(codigoField != null && codigoField.getText() != null ? codigoField.getText().trim() : "");
        grupo.setNombre(nombreField != null && nombreField.getText() != null ? nombreField.getText().trim() : "");
        grupo.setGrado(gradoField != null && gradoField.getText() != null ? gradoField.getText().trim() : "");
        grupo.setSeccion(seccionField != null && seccionField.getText() != null ? seccionField.getText().trim() : "");
        
        if (profesorTitularCombo != null && profesorTitularCombo.getValue() != null) {
            grupo.setProfesorTitularId(profesorTitularCombo.getValue().getId());
        }
        
        grupo.setActivo("Activo".equals(estatusCombo != null ? estatusCombo.getValue() : "Activo"));

        return grupo;
    }

    private void mostrarAsignacionAlumnos() {
        Grupo grupoSeleccionado = tablaGrupos.getSelectionModel().getSelectedItem();
        if (grupoSeleccionado == null) return;

        // Crear diálogo de asignación de alumnos
        Dialog<List<Alumno>> dialog = new Dialog<>();
        dialog.setTitle("Asignar Alumnos al Grupo: " + grupoSeleccionado.getNombre());
        dialog.setHeaderText("Seleccione los alumnos que pertenecerán a este grupo");

        ButtonType asignarButtonType = new ButtonType("Asignar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(asignarButtonType, ButtonType.CANCEL);

        // Lista de alumnos disponibles con checkboxes
        VBox alumnosContainer = new VBox(5);
        List<CheckBox> checkBoxesAlumnos = new ArrayList<>();

        // Cargar alumnos
        List<Alumno> alumnos = AlumnoController.obtenerAlumnos();
        for (Alumno alumno : alumnos) {
            CheckBox checkBox = new CheckBox(alumno.getNombreCompleto() + " (" + alumno.getMatricula() + ")");
            checkBox.setUserData(alumno);
            checkBoxesAlumnos.add(checkBox);
            alumnosContainer.getChildren().add(checkBox);
        }

        // Seleccionar alumnos ya asignados
        if (grupoSeleccionado.getAlumnosIds() != null) {
            for (ObjectId alumnoId : grupoSeleccionado.getAlumnosIds()) {
                checkBoxesAlumnos.stream()
                        .filter(cb -> ((Alumno) cb.getUserData()).getId().equals(alumnoId))
                        .findFirst()
                        .ifPresent(cb -> cb.setSelected(true));
            }
        }

        ScrollPane scrollPane = new ScrollPane(alumnosContainer);
        scrollPane.setPrefSize(400, 300);
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Alumnos disponibles:"),
                scrollPane
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == asignarButtonType) {
                List<Alumno> alumnosSeleccionados = new ArrayList<>();
                for (CheckBox checkBox : checkBoxesAlumnos) {
                    if (checkBox.isSelected()) {
                        alumnosSeleccionados.add((Alumno) checkBox.getUserData());
                    }
                }
                return alumnosSeleccionados;
            }
            return null;
        });

        Optional<List<Alumno>> resultado = dialog.showAndWait();
        resultado.ifPresent(alumnosSeleccionados -> {
            try {
                List<ObjectId> alumnosIds = new ArrayList<>();
                for (Alumno alumno : alumnosSeleccionados) {
                    alumnosIds.add(alumno.getId());
                }
                
                boolean exito = GrupoController.asignarAlumnosAGrupo(grupoSeleccionado.getId(), alumnosIds);
                if (exito) {
                    mostrarInfo("Alumnos asignados exitosamente al grupo");
                    cargarGrupos();
                } else {
                    mostrarError("Error al asignar alumnos al grupo");
                }
            } catch (Exception e) {
                mostrarError("Error al asignar alumnos: " + e.getMessage());
            }
        });
    }

    private void mostrarAsignacionMaterias() {
        Grupo grupoSeleccionado = tablaGrupos.getSelectionModel().getSelectedItem();
        if (grupoSeleccionado == null) return;

        // Crear diálogo de asignación de materias
        Dialog<List<Materia>> dialog = new Dialog<>();
        dialog.setTitle("Asignar Materias al Grupo: " + grupoSeleccionado.getNombre());
        dialog.setHeaderText("Seleccione las materias que se impartirán en este grupo");

        ButtonType asignarButtonType = new ButtonType("Asignar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(asignarButtonType, ButtonType.CANCEL);

        // Lista de materias disponibles con checkboxes
        VBox materiasContainer = new VBox(5);
        List<CheckBox> checkBoxesMaterias = new ArrayList<>();

        // Cargar materias
        List<Materia> materias = MateriaController.obtenerMaterias();
        for (Materia materia : materias) {
            CheckBox checkBox = new CheckBox(materia.getNombre() + " (" + materia.getCodigo() + ")");
            checkBox.setUserData(materia);
            checkBoxesMaterias.add(checkBox);
            materiasContainer.getChildren().add(checkBox);
        }

        // Seleccionar materias ya asignadas
        if (grupoSeleccionado.getMateriasIds() != null) {
            for (ObjectId materiaId : grupoSeleccionado.getMateriasIds()) {
                checkBoxesMaterias.stream()
                        .filter(cb -> ((Materia) cb.getUserData()).getId().equals(materiaId))
                        .findFirst()
                        .ifPresent(cb -> cb.setSelected(true));
            }
        }

        ScrollPane scrollPane = new ScrollPane(materiasContainer);
        scrollPane.setPrefSize(400, 300);
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Materias disponibles:"),
                scrollPane
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == asignarButtonType) {
                List<Materia> materiasSeleccionadas = new ArrayList<>();
                for (CheckBox checkBox : checkBoxesMaterias) {
                    if (checkBox.isSelected()) {
                        materiasSeleccionadas.add((Materia) checkBox.getUserData());
                    }
                }
                return materiasSeleccionadas;
            }
            return null;
        });

        Optional<List<Materia>> resultado = dialog.showAndWait();
        resultado.ifPresent(materiasSeleccionadas -> {
            try {
                List<ObjectId> materiasIds = new ArrayList<>();
                for (Materia materia : materiasSeleccionadas) {
                    materiasIds.add(materia.getId());
                }
                
                boolean exito = GrupoController.asignarMateriasAGrupo(grupoSeleccionado.getId(), materiasIds);
                if (exito) {
                    mostrarInfo("Materias asignadas exitosamente al grupo");
                    cargarGrupos();
                } else {
                    mostrarError("Error al asignar materias al grupo");
                }
            } catch (Exception e) {
                mostrarError("Error al asignar materias: " + e.getMessage());
            }
        });
    }

    private void eliminarGrupo() {
        Grupo seleccionado = tablaGrupos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar este grupo?");
        confirmacion.setContentText("Grupo: " + seleccionado.getNombre() + " (" + seleccionado.getCodigo() + ")");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                GrupoController.eliminarGrupo(seleccionado.getId());
                mostrarInfo("Grupo eliminado exitosamente");
                cargarGrupos();
            } catch (Exception e) {
                mostrarError("Error al eliminar grupo: " + e.getMessage());
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