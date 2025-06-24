// DatabaseConfig.java
package com.controlescolar.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class DatabaseConfig {
    private static DatabaseConfig instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

    // Configuración de conexión
    private static final String HOST = "localhost";
    private static final int PORT = 27017;
    private static final String DATABASE_NAME = "control_escolar";

    // Para MongoDB Atlas (comentado por defecto)
    // private static final String CONNECTION_STRING = "mongodb+srv://username:password@cluster.mongodb.net/control_escolar?retryWrites=true&w=majority";

    private DatabaseConfig() {
        connect();
    }

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private void connect() {
        try {
            // Conexión local usando nuevo driver
            String connectionString = "mongodb://" + HOST + ":" + PORT;
            mongoClient = MongoClients.create(connectionString);

            // Para MongoDB Atlas, usar esta línea en su lugar:
            // mongoClient = MongoClients.create(CONNECTION_STRING);

            database = mongoClient.getDatabase(DATABASE_NAME);

            // Verificar conexión
            database.runCommand(new Document("ping", 1));
            System.out.println("✅ Conexión exitosa a MongoDB");

        } catch (Exception e) {
            System.err.println("❌ Error al conectar con MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    // Métodos helper para obtener colecciones
    public MongoCollection<Document> getUsuariosCollection() {
        return database.getCollection("usuarios");
    }

    public MongoCollection<Document> getAlumnosCollection() {
        return database.getCollection("alumnos");
    }

    public MongoCollection<Document> getProfesoresCollection() {
        return database.getCollection("profesores");
    }

    public MongoCollection<Document> getMateriasCollection() {
        return database.getCollection("materias");
    }

    public MongoCollection<Document> getGruposCollection() {
        return database.getCollection("grupos");
    }

    public MongoCollection<Document> getCalificacionesCollection() {
        return database.getCollection("calificaciones");
    }

    public MongoCollection<Document> getAsistenciaCollection() {
        return database.getCollection("asistencia");
    }

    public MongoCollection<Document> getPagosCollection() {
        return database.getCollection("pagos");
    }

    // Cerrar conexión
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("🔌 Conexión a MongoDB cerrada");
        }
    }

    // Método para verificar si la conexión está activa
    public boolean isConnected() {
        try {
            database.runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}