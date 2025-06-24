//Databaseutil.java
package com.controlescolar.utils;

import com.controlescolar.config.DatabaseConfig;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {
    private static final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    // Crear documento
    public static void insertDocument(String collectionName, Document document) {
        try {
            MongoCollection<Document> collection = dbConfig.getDatabase().getCollection(collectionName);
            collection.insertOne(document);
            System.out.println("✅ Documento insertado en " + collectionName);
        } catch (Exception e) {
            System.err.println("❌ Error al insertar documento: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Buscar documento por ID
    public static Document findDocumentById(String collectionName, String id) {
        try {
            MongoCollection<Document> collection = dbConfig.getDatabase().getCollection(collectionName);
            return collection.find(Filters.eq("_id", new ObjectId(id))).first();
        } catch (Exception e) {
            System.err.println("❌ Error al buscar documento: " + e.getMessage());
            return null;
        }
    }

    // Buscar documento por campo
    public static Document findDocumentByField(String collectionName, String field, Object value) {
        try {
            MongoCollection<Document> collection = dbConfig.getDatabase().getCollection(collectionName);
            return collection.find(Filters.eq(field, value)).first();
        } catch (Exception e) {
            System.err.println("❌ Error al buscar documento: " + e.getMessage());
            return null;
        }
    }

    // Obtener todos los documentos
    public static List<Document> findAllDocuments(String collectionName) {
        List<Document> documents = new ArrayList<>();
        try {
            MongoCollection<Document> collection = dbConfig.getDatabase().getCollection(collectionName);
            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    documents.add(cursor.next());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error al obtener documentos: " + e.getMessage());
        }
        return documents;
    }

    // Actualizar documento
    public static boolean updateDocument(String collectionName, String id, Document updates) {
        try {
            MongoCollection<Document> collection = dbConfig.getDatabase().getCollection(collectionName);
            UpdateResult result = collection.updateOne(
                    Filters.eq("_id", new ObjectId(id)),
                    new Document("$set", updates)
            );
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar documento: " + e.getMessage());
            return false;
        }
    }

    // Eliminar documento
    public static boolean deleteDocument(String collectionName, String id) {
        try {
            MongoCollection<Document> collection = dbConfig.getDatabase().getCollection(collectionName);
            DeleteResult result = collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar documento: " + e.getMessage());
            return false;
        }
    }

    // Buscar documentos con filtros múltiples
    public static List<Document> findDocumentsWithFilters(String collectionName, Document filters) {
        List<Document> documents = new ArrayList<>();
        try {
            MongoCollection<Document> collection = dbConfig.getDatabase().getCollection(collectionName);
            try (MongoCursor<Document> cursor = collection.find(filters).iterator()) {
                while (cursor.hasNext()) {
                    documents.add(cursor.next());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error al buscar documentos con filtros: " + e.getMessage());
        }
        return documents;
    }

    // Contar documentos
    public static long countDocuments(String collectionName) {
        try {
            MongoCollection<Document> collection = dbConfig.getDatabase().getCollection(collectionName);
            return collection.countDocuments();
        } catch (Exception e) {
            System.err.println("❌ Error al contar documentos: " + e.getMessage());
            return 0;
        }
    }

    // Verificar si existe un documento
    public static boolean documentExists(String collectionName, String field, Object value) {
        try {
            MongoCollection<Document> collection = dbConfig.getDatabase().getCollection(collectionName);
            return collection.find(Filters.eq(field, value)).first() != null;
        } catch (Exception e) {
            System.err.println("❌ Error al verificar existencia: " + e.getMessage());
            return false;
        }
    }

    // Obtener colección directamente
    public static MongoCollection<Document> getCollection(String collectionName) {
        return dbConfig.getDatabase().getCollection(collectionName);
    }
}