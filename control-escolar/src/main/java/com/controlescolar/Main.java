// Main.java
package com.controlescolar;

import com.controlescolar.config.DatabaseConfig;
import com.controlescolar.views.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Verificar conexión a la base de datos al iniciar
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            if (dbConfig.isConnected()) {
                System.out.println("🎉 Sistema iniciado correctamente");

                // Crear datos iniciales si es necesario
                createInitialData();

                // Iniciar la aplicación JavaFX
                LoginView loginView = new LoginView();
                loginView.start(primaryStage);

            } else {
                System.err.println("❌ No se pudo conectar a la base de datos");
                System.exit(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        // Cerrar conexión al salir
        DatabaseConfig.getInstance().close();
        System.out.println("👋 Aplicación cerrada correctamente");
    }

    private void createInitialData() {
        // Aquí puedes crear datos iniciales como el usuario administrador
        try {
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();

            // Verificar si ya existe un usuario administrador
            org.bson.Document adminUser = dbConfig.getUsuariosCollection()
                    .find(new org.bson.Document("email", "admin@controlescolar.com"))
                    .first();

            if (adminUser == null) {
                // Crear usuario administrador por defecto
                org.bson.Document admin = new org.bson.Document()
                        .append("email", "admin@controlescolar.com")
                        .append("password", org.mindrot.jbcrypt.BCrypt.hashpw("admin123", org.mindrot.jbcrypt.BCrypt.gensalt()))
                        .append("rol", "ADMINISTRADOR")
                        .append("nombre", "Administrador")
                        .append("apellido", "Sistema")
                        .append("activo", true)
                        .append("fechaCreacion", java.time.LocalDateTime.now().toString());

                dbConfig.getUsuariosCollection().insertOne(admin);
                System.out.println("👤 Usuario administrador creado: admin@controlescolar.com / admin123");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error al crear datos iniciales: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}