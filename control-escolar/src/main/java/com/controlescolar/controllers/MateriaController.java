// MateriaController.java
package com.controlescolar.controllers;

import com.controlescolar.models.Materia;
import com.controlescolar.utils.DatabaseUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

public class MateriaController {
    private static MongoCollection<Document> collection = DatabaseUtil.getCollection("materias");

    public static boolean crearMateria(Materia materia) {
        try {
            // Verificar si el código ya existe
            if (collection.find(Filters.eq("codigo", materia.getCodigo())).first() != null) {
                return false; // Código ya existe
            }

            collection.insertOne(materia.toDocument());
            return true;
        } catch (Exception e) {
            System.err.println("Error al crear materia: " + e.getMessage());
            return false;
        }
    }

    public static boolean actualizarMateria(Materia materia) {
        try {
            Document updateDoc = new Document()
                    .append("nombre", materia.getNombre())
                    .append("descripcion", materia.getDescripcion())
                    .append("creditos", materia.getCreditos())
                    .append("horasSemanales", materia.getHorasSemanales())
                    .append("prerrequisitos", materia.getPrerrequisitos())
                    .append("activo", materia.isActivo());

            collection.updateOne(
                    Filters.eq("_id", materia.getId()),
                    new Document("$set", updateDoc)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar materia: " + e.getMessage());
            return false;
        }
    }

    public static List<Materia> obtenerMaterias() {
        List<Materia> materias = new ArrayList<>();
        try {
            collection.find(Filters.eq("activo", true))
                    .forEach(doc -> materias.add(Materia.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener materias: " + e.getMessage());
        }
        return materias;
    }

    public static Materia obtenerMateriaPorId(ObjectId id) {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
            return doc != null ? Materia.fromDocument(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener materia por ID: " + e.getMessage());
            return null;
        }
    }

    public static Materia obtenerMateriaPorCodigo(String codigo) {
        try {
            Document doc = collection.find(Filters.eq("codigo", codigo)).first();
            return doc != null ? Materia.fromDocument(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener materia por código: " + e.getMessage());
            return null;
        }
    }

    public static List<Materia> buscarMaterias(String termino) {
        List<Materia> materias = new ArrayList<>();
        try {
            Document regex = new Document("$regex", termino).append("$options", "i");
            Document filter = new Document("$or", List.of(
                    new Document("nombre", regex),
                    new Document("codigo", regex),
                    new Document("descripcion", regex)
            )).append("activo", true);

            collection.find(filter).forEach(doc -> materias.add(Materia.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al buscar materias: " + e.getMessage());
        }
        return materias;
    }

    public static boolean eliminarMateria(ObjectId id) {
        try {
            collection.updateOne(
                    Filters.eq("_id", id),
                    Updates.set("activo", false)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar materia: " + e.getMessage());
            return false;
        }
    }
}