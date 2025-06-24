// AuthController.java
package com.controlescolar.controllers;

import com.controlescolar.models.Usuario;
import com.controlescolar.utils.DatabaseUtil;
import com.controlescolar.utils.SecurityUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.time.LocalDateTime;

public class AuthController {
    private static Usuario usuarioActual;
    private static MongoCollection<Document> collection = DatabaseUtil.getCollection("usuarios");

    public static boolean login(String email, String password) {
        try {
            Document userDoc = collection.find(Filters.eq("email", email)).first();

            if (userDoc != null && SecurityUtil.verifyPassword(password, userDoc.getString("password"))) {
                usuarioActual = Usuario.fromDocument(userDoc);
                usuarioActual.setUltimoAcceso(LocalDateTime.now());

                // Actualizar último acceso
                collection.updateOne(
                        Filters.eq("_id", usuarioActual.getId()),
                        new Document("$set", new Document("ultimoAcceso", LocalDateTime.now()))
                );

                return true;
            }
        } catch (Exception e) {
            System.err.println("Error en login: " + e.getMessage());
        }
        return false;
    }

    public static void logout() {
        usuarioActual = null;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static boolean isLoggedIn() {
        return usuarioActual != null;
    }

    public static boolean hasRole(String role) {
        return usuarioActual != null && usuarioActual.getRol().toString().equals(role);
    }
}