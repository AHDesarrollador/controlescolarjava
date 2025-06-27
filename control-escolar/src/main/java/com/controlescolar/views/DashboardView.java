//DashBoardView.java
package com.controlescolar.views;

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
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardView extends Application {

    private Usuario usuarioActual;
    private Stage primaryStage;
    private BorderPane mainLayout;
    private VBox sideMenu;
    private VBox contentArea;
    private Label bienvenidaLabel;
    private Label fechaHoraLabel;

    public DashboardView(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Configuración de la ventana
        primaryStage.setTitle("Sistema Control Escolar - Dashboard");
        primaryStage.setMaximized(true);

        // Layout principal
        mainLayout = new BorderPane();

        // Crear componentes
        createTopBar();
        createSideMenu();
        createContentArea();
        createStatusBar();

        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        // Mostrar vista inicial
        mostrarVistaInicial();
    }

    private void createTopBar() {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #2c3e50;");
        topBar.setPrefHeight(60);

        // Logo/Título
        Label titulo = new Label("Sistema Control Escolar");
        titulo.setTextFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Info usuario
        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        bienvenidaLabel = new Label("Bienvenido, " + usuarioActual.getNombre());
        bienvenidaLabel.setTextFill(Color.WHITE);
        bienvenidaLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        Label rolLabel = new Label("Rol: " + usuarioActual.getRol().toString());
        rolLabel.setTextFill(Color.LIGHTGRAY);
        rolLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));

        userInfo.getChildren().addAll(bienvenidaLabel, rolLabel);

        // Botón cerrar sesión
        Button logoutButton = new Button("Cerrar Sesión");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 3;");
        logoutButton.setOnAction(e -> cerrarSesion());

        topBar.getChildren().addAll(titulo, spacer, userInfo, logoutButton);
        mainLayout.setTop(topBar);
    }

    private void createSideMenu() {
        sideMenu = new VBox(5);
        sideMenu.setPadding(new Insets(20, 10, 20, 10));
        sideMenu.setStyle("-fx-background-color: #34495e;");
        sideMenu.setPrefWidth(250);

        // Título del menú
        Label menuTitle = new Label("Menú Principal");
        menuTitle.setTextFill(Color.WHITE);
        menuTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        menuTitle.setPadding(new Insets(0, 0, 15, 0));

        sideMenu.getChildren().add(menuTitle);

        // Crear botones de menú según el rol
        createMenuButtons();

        mainLayout.setLeft(sideMenu);
    }

    private void createMenuButtons() {
        Rol rol = usuarioActual.getRol();

        // Botones comunes
        addMenuButton("🏠 Inicio", () -> mostrarVistaInicial());
        addMenuButton("👤 Mi Perfil", () -> abrirPerfil());

        // Botones según rol
        if (rol == Rol.ADMINISTRADOR) {
            addMenuButton("👥 Gestión de Usuarios", () -> abrirGestionUsuarios());
            addMenuButton("🎓 Gestión de Alumnos", () -> abrirGestionAlumnos());
            addMenuButton("👨‍🏫 Gestión de Profesores", () -> abrirGestionProfesores());
            addMenuButton("👨‍👩‍👧‍👦 Gestión de Padres", () -> abrirGestionPadres());
            addMenuButton("📚 Gestión de Materias", () -> abrirGestionMaterias());
            addMenuButton("👥 Gestión de Grupos", () -> abrirGestionGrupos());
            addMenuButton("📊 Calificaciones", () -> abrirCalificaciones());
            addMenuButton("📅 Asistencias", () -> abrirAsistencias());
            addMenuButton("💰 Pagos", () -> abrirPagos());
            addMenuButton("📈 Reportes", () -> abrirReportes());
        } else if (rol == Rol.DIRECTOR) {
            addMenuButton("👤 Gestión de Usuarios", () -> abrirGestionUsuariosDirector());
            addMenuButton("🎓 Gestión de Alumnos", () -> abrirGestionAlumnos());
            addMenuButton("👨‍🏫 Gestión de Profesores", () -> abrirGestionProfesores());
            addMenuButton("👨‍👩‍👧‍👦 Gestión de Padres", () -> abrirGestionPadres());
            addMenuButton("📚 Gestión de Materias", () -> abrirGestionMaterias());
            addMenuButton("👥 Gestión de Grupos", () -> abrirGestionGrupos());
            addMenuButton("📊 Calificaciones", () -> abrirCalificaciones());
            addMenuButton("📅 Asistencias", () -> abrirAsistencias());
            addMenuButton("💰 Pagos", () -> abrirPagos());
            addMenuButton("📈 Reportes", () -> abrirReportes());
        } else if (rol == Rol.SECRETARIO) {
            addMenuButton("🎓 Gestión de Alumnos", () -> abrirGestionAlumnos());
            addMenuButton("👨‍🏫 Gestión de Profesores", () -> abrirGestionProfesores());
            addMenuButton("👨‍👩‍👧‍👦 Gestión de Padres", () -> abrirGestionPadres());
            addMenuButton("👤 Editar Usuarios", () -> abrirEdicionUsuarios());
            addMenuButton("💰 Pagos", () -> abrirPagos());
            addMenuButton("📈 Reportes", () -> abrirReportes());
        } else if (rol == Rol.PROFESOR) {
            addMenuButton("👥 Mis Grupos", () -> abrirMisGrupos());
            addMenuButton("📊 Calificaciones", () -> abrirCalificaciones());
            addMenuButton("📅 Asistencias", () -> abrirAsistencias());
            addMenuButton("📋 Reportes", () -> abrirReportes());
        } else if (rol == Rol.ALUMNO) {
            addMenuButton("📊 Mis Calificaciones", () -> abrirMisCalificaciones());
            addMenuButton("📅 Mi Asistencia", () -> abrirMiAsistencia());
            addMenuButton("💰 Mis Pagos", () -> abrirMisPagos());
        }
    }

    private void addMenuButton(String texto, Runnable accion) {
        Button button = new Button(texto);
        button.setPrefWidth(220);
        button.setPrefHeight(40);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12px; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Efectos hover
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-border-radius: 5; -fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12px; -fx-border-radius: 5; -fx-background-radius: 5;"));

        button.setOnAction(e -> accion.run());

        sideMenu.getChildren().add(button);
    }

    private void createContentArea() {
        contentArea = new VBox();
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color: #ecf0f1;");

        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");

        mainLayout.setCenter(scrollPane);
    }

    private void createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5, 20, 5, 20));
        statusBar.setAlignment(Pos.CENTER_RIGHT);
        statusBar.setStyle("-fx-background-color: #95a5a6;");
        statusBar.setPrefHeight(25);

        fechaHoraLabel = new Label();
        fechaHoraLabel.setTextFill(Color.WHITE);
        fechaHoraLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        updateDateTime();

        // Actualizar fecha/hora cada segundo
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDateTime()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        statusBar.getChildren().add(fechaHoraLabel);
        mainLayout.setBottom(statusBar);
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        fechaHoraLabel.setText("Fecha y hora: " + now.format(formatter));
    }

    private void mostrarVistaInicial() {
        contentArea.getChildren().clear();

        VBox welcomeBox = new VBox(20);
        welcomeBox.setAlignment(Pos.CENTER);
        welcomeBox.setPadding(new Insets(50));

        // Título de bienvenida
        Label welcomeTitle = new Label("¡Bienvenido al Sistema de Control Escolar!");
        welcomeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        welcomeTitle.setTextFill(Color.valueOf("#2c3e50"));

        // Información del usuario
        VBox userInfoBox = new VBox(10);
        userInfoBox.setAlignment(Pos.CENTER);
        userInfoBox.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label userLabel = new Label("Usuario: " + usuarioActual.getNombre());
        userLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        Label rolLabel = new Label("Rol: " + usuarioActual.getRol().toString());
        rolLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        Label emailLabel = new Label("Email: " + usuarioActual.getEmail());
        emailLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        userInfoBox.getChildren().addAll(userLabel, rolLabel, emailLabel);

        // Cards de estadísticas (solo para admin y profesores)
        if (usuarioActual.getRol() != Rol.ALUMNO) {
            HBox statsBox = createStatsCards();
            welcomeBox.getChildren().addAll(welcomeTitle, userInfoBox, statsBox);
        } else {
            welcomeBox.getChildren().addAll(welcomeTitle, userInfoBox);
        }

        contentArea.getChildren().add(welcomeBox);
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        // Obtener estadísticas reales
        try {
            int totalAlumnos = com.controlescolar.controllers.AlumnoController.obtenerAlumnos().size();
            int totalProfesores = com.controlescolar.controllers.ProfesorController.obtenerProfesores().size();
            int totalMaterias = com.controlescolar.controllers.MateriaController.obtenerMaterias().size();
            int totalGrupos = com.controlescolar.controllers.GrupoController.obtenerTodosLosGrupos().size();

            // Card de alumnos
            VBox alumnosCard = createStatCard("Alumnos", String.valueOf(totalAlumnos), "#3498db");

            // Card de profesores
            VBox profesoresCard = createStatCard("Profesores", String.valueOf(totalProfesores), "#2ecc71");

            // Card de materias
            VBox materiasCard = createStatCard("Materias", String.valueOf(totalMaterias), "#e74c3c");

            // Card de grupos
            VBox gruposCard = createStatCard("Grupos", String.valueOf(totalGrupos), "#f39c12");

            statsBox.getChildren().addAll(alumnosCard, profesoresCard, materiasCard, gruposCard);
        } catch (Exception e) {
            // En caso de error, mostrar datos por defecto
            VBox alumnosCard = createStatCard("Alumnos", "---", "#3498db");
            VBox profesoresCard = createStatCard("Profesores", "---", "#2ecc71");
            VBox materiasCard = createStatCard("Materias", "---", "#e74c3c");
            VBox gruposCard = createStatCard("Grupos", "---", "#f39c12");

            statsBox.getChildren().addAll(alumnosCard, profesoresCard, materiasCard, gruposCard);
            System.err.println("Error al obtener estadísticas del dashboard: " + e.getMessage());
        }

        return statsBox;
    }

    private VBox createStatCard(String titulo, String valor, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(150);
        card.setPrefHeight(100);
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        Label tituloLabel = new Label(titulo);
        tituloLabel.setTextFill(Color.WHITE);
        tituloLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        Label valorLabel = new Label(valor);
        valorLabel.setTextFill(Color.WHITE);
        valorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        card.getChildren().addAll(tituloLabel, valorLabel);

        return card;
    }

    // Métodos para abrir diferentes vistas
    private void abrirGestionUsuarios() {
        try {
            GestionUsuariosView gestionUsuariosView = new GestionUsuariosView(usuarioActual);
            gestionUsuariosView.show();
        } catch (Exception e) {
            mostrarError("Error al abrir gestión de usuarios: " + e.getMessage());
        }
    }

    private void abrirGestionAlumnos() {
        try {
            AlumnosView alumnosView = new AlumnosView(usuarioActual);
            Stage alumnosStage = new Stage();
            alumnosView.start(alumnosStage);
        } catch (Exception e) {
            mostrarError("Error al abrir gestión de alumnos: " + e.getMessage());
        }
    }

    private void abrirGestionProfesores() {
        try {
            ProfesoresView profesoresView = new ProfesoresView(usuarioActual);
            Stage profesoresStage = new Stage();
            profesoresView.start(profesoresStage);
        } catch (Exception e) {
            mostrarError("Error al abrir gestión de profesores: " + e.getMessage());
        }
    }

    private void abrirGestionMaterias() {
        try {
            MateriasView materiasView = new MateriasView(usuarioActual);
            Stage materiasStage = new Stage();
            materiasView.start(materiasStage);
        } catch (Exception e) {
            mostrarError("Error al abrir gestión de materias: " + e.getMessage());
        }
    }

    private void abrirCalificaciones() {
        try {
            CalificacionesView calificacionesView = new CalificacionesView(usuarioActual);
            Stage calificacionesStage = new Stage();
            calificacionesView.start(calificacionesStage);
        } catch (Exception e) {
            mostrarError("Error al abrir calificaciones: " + e.getMessage());
        }
    }

    private void abrirAsistencias() {
        try {
            Stage asistenciaStage = new Stage();
            AsistenciaView asistenciaView = new AsistenciaView(asistenciaStage);
            asistenciaView.show();
        } catch (Exception e) {
            mostrarError("Error al abrir asistencias: " + e.getMessage());
        }
    }

    private void abrirPagos() {
        try {
            Stage pagosStage = new Stage();
            PagosView pagosView = new PagosView(pagosStage);
            pagosView.show();
        } catch (Exception e) {
            mostrarError("Error al abrir pagos: " + e.getMessage());
        }
    }

    private void abrirReportes() {
        try {
            ReportesView reportesView = new ReportesView();
            Stage reportesStage = new Stage();
            reportesView.start(reportesStage);
        } catch (Exception e) {
            mostrarError("Error al abrir reportes: " + e.getMessage());
        }
    }

    // Métodos específicos para profesor
    private void abrirMisGrupos() {
        try {
            MisGruposView misGruposView = new MisGruposView(usuarioActual);
            Stage misGruposStage = new Stage();
            misGruposView.start(misGruposStage);
        } catch (Exception e) {
            mostrarError("Error al abrir mis grupos: " + e.getMessage());
        }
    }

    // Métodos específicos para alumno
    private void abrirMisCalificaciones() {
        try {
            MisCalificacionesView misCalificacionesView = new MisCalificacionesView(usuarioActual);
            misCalificacionesView.show();
        } catch (Exception e) {
            mostrarError("Error al abrir mis calificaciones: " + e.getMessage());
        }
    }

    private void abrirMiAsistencia() {
        try {
            MiAsistenciaView miAsistenciaView = new MiAsistenciaView(usuarioActual);
            miAsistenciaView.show();
        } catch (Exception e) {
            mostrarError("Error al abrir mi asistencia: " + e.getMessage());
        }
    }

    private void abrirMisPagos() {
        try {
            MisPagosView misPagosView = new MisPagosView(usuarioActual);
            misPagosView.show();
        } catch (Exception e) {
            mostrarError("Error al abrir mis pagos: " + e.getMessage());
        }
    }

    private void abrirPerfil() {
        try {
            com.controlescolar.views.ProfileView profileView = new com.controlescolar.views.ProfileView(usuarioActual);
            profileView.show();
        } catch (Exception e) {
            mostrarError("Error al abrir el perfil: " + e.getMessage());
        }
    }

    private void abrirGestionGrupos() {
        try {
            GruposView gruposView = new GruposView(usuarioActual);
            Stage gruposStage = new Stage();
            gruposView.start(gruposStage);
        } catch (Exception e) {
            mostrarError("Error al abrir gestión de grupos: " + e.getMessage());
        }
    }

    private void abrirGestionPadres() {
        try {
            GestionPadresView gestionPadresView = new GestionPadresView(primaryStage);
            gestionPadresView.show();
        } catch (Exception e) {
            mostrarError("Error al abrir gestión de padres: " + e.getMessage());
        }
    }

    private void abrirEdicionUsuarios() {
        try {
            EdicionUsuariosView edicionUsuariosView = new EdicionUsuariosView(primaryStage);
            edicionUsuariosView.show();
        } catch (Exception e) {
            mostrarError("Error al abrir edición de usuarios: " + e.getMessage());
        }
    }

    private void abrirGestionUsuariosDirector() {
        try {
            GestionUsuariosDirectorView gestionUsuariosView = new GestionUsuariosDirectorView(primaryStage);
            gestionUsuariosView.show();
        } catch (Exception e) {
            mostrarError("Error al abrir gestión de usuarios: " + e.getMessage());
        }
    }

    private void mostrarMensajeDesarrollo(String modulo) {
        contentArea.getChildren().clear();

        VBox messageBox = new VBox(20);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(50));

        Label messageLabel = new Label("Módulo: " + modulo);
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        messageLabel.setTextFill(Color.valueOf("#2c3e50"));

        Label devLabel = new Label("En desarrollo...");
        devLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        devLabel.setTextFill(Color.GRAY);

        messageBox.getChildren().addAll(messageLabel, devLabel);
        contentArea.getChildren().add(messageBox);
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cerrarSesion() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cerrar Sesión");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("¿Estás seguro que deseas cerrar sesión?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                LoginView loginView = new LoginView();
                Stage loginStage = new Stage();
                loginView.start(loginStage);

                primaryStage.close();
            } catch (Exception e) {
                mostrarError("Error al cerrar sesión: " + e.getMessage());
            }
        }
    }
}