// ProfileController.java
package com.controlescolar.controllers;

import com.controlescolar.models.Usuario;
import com.controlescolar.utils.DatabaseUtil;
import com.controlescolar.utils.SecurityUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class ProfileController {
    private static MongoCollection<Document> collection = DatabaseUtil.getCollection("usuarios");
    private static final String FOTO_DIRECTORY = "fotos/usuarios/";

    public static boolean actualizarPerfil(Usuario usuario, String nombre, String apellidos, String telefono) {
        try {
            Document updateDoc = new Document()
                    .append("nombre", nombre)
                    .append("apellidos", apellidos)
                    .append("telefono", telefono);

            collection.updateOne(
                    Filters.eq("_id", usuario.getId()),
                    new Document("$set", updateDoc)
            );

            // Actualizar objeto en memoria
            usuario.setNombre(nombre);
            usuario.setApellidos(apellidos);
            usuario.setTelefono(telefono);

            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar perfil: " + e.getMessage());
            return false;
        }
    }

    public static boolean cambiarPassword(Usuario usuario, String passwordActual, String passwordNueva) {
        try {
            Document userDoc = collection.find(Filters.eq("_id", usuario.getId())).first();

            if (userDoc != null && SecurityUtil.verifyPassword(passwordActual, userDoc.getString("password"))) {
                String hashedPassword = SecurityUtil.hashPassword(passwordNueva);
                collection.updateOne(
                        Filters.eq("_id", usuario.getId()),
                        Updates.set("password", hashedPassword)
                );
                
                usuario.setPassword(hashedPassword);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error al cambiar contraseña: " + e.getMessage());
        }
        return false;
    }

    public static String subirFoto(Usuario usuario, Stage parentStage) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Foto de Perfil");
            
            // Filtros para imágenes
            FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                    "Archivos de Imagen", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp");
            fileChooser.getExtensionFilters().add(imageFilter);

            File selectedFile = fileChooser.showOpenDialog(parentStage);
            
            if (selectedFile != null) {
                return guardarFoto(usuario, selectedFile);
            }
        } catch (Exception e) {
            System.err.println("Error al seleccionar foto: " + e.getMessage());
        }
        return null;
    }

    private static String guardarFoto(Usuario usuario, File archivoFoto) {
        try {
            // Crear directorio si no existe
            Path directorioFotos = Paths.get(FOTO_DIRECTORY);
            if (!Files.exists(directorioFotos)) {
                Files.createDirectories(directorioFotos);
            }

            // Generar nombre único para la foto
            String extension = getFileExtension(archivoFoto.getName());
            String nombreFoto = usuario.getId().toString() + "_" + UUID.randomUUID().toString() + "." + extension;
            Path rutaDestino = directorioFotos.resolve(nombreFoto);

            // Copiar archivo
            Files.copy(archivoFoto.toPath(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

            // Actualizar base de datos
            String rutaFoto = FOTO_DIRECTORY + nombreFoto;
            collection.updateOne(
                    Filters.eq("_id", usuario.getId()),
                    Updates.set("foto", rutaFoto)
            );

            // Eliminar foto anterior si existe
            if (usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
                eliminarFotoAnterior(usuario.getFoto());
            }

            usuario.setFoto(rutaFoto);
            return rutaFoto;

        } catch (IOException e) {
            System.err.println("Error al guardar foto: " + e.getMessage());
            return null;
        }
    }

    private static void eliminarFotoAnterior(String rutaFoto) {
        try {
            Path fotoAnterior = Paths.get(rutaFoto);
            if (Files.exists(fotoAnterior)) {
                Files.delete(fotoAnterior);
            }
        } catch (IOException e) {
            System.err.println("Error al eliminar foto anterior: " + e.getMessage());
        }
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg";
    }

    public static boolean eliminarFoto(Usuario usuario) {
        try {
            if (usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
                eliminarFotoAnterior(usuario.getFoto());
                
                collection.updateOne(
                        Filters.eq("_id", usuario.getId()),
                        Updates.unset("foto")
                );
                
                usuario.setFoto(null);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error al eliminar foto: " + e.getMessage());
        }
        return false;
    }

    public static Usuario obtenerPerfilCompleto(ObjectId usuarioId) {
        try {
            Document doc = collection.find(Filters.eq("_id", usuarioId)).first();
            return doc != null ? Usuario.fromDocument(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener perfil: " + e.getMessage());
            return null;
        }
    }

    public static boolean validarPassword(String password) {
        // Validaciones de seguridad para contraseñas
        if (password == null || password.length() < 6) {
            return false;
        }
        
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        return hasUpper && hasLower && hasDigit;
    }

    public static String getPasswordRequirements() {
        return "La contraseña debe tener al menos 6 caracteres, incluir mayúsculas, minúsculas y números.";
    }
}