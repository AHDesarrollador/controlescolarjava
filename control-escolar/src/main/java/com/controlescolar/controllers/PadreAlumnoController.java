package com.controlescolar.controllers;

import com.controlescolar.models.PadreAlumno;
import com.controlescolar.models.Alumno;
import com.controlescolar.models.Usuario;
import com.controlescolar.enums.Rol;
import com.controlescolar.utils.DatabaseUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

public class PadreAlumnoController {
    private static MongoCollection<Document> collection = DatabaseUtil.getCollection("padres_alumnos");
    private static MongoCollection<Document> usuariosCollection = DatabaseUtil.getCollection("usuarios");
    private static MongoCollection<Document> alumnosCollection = DatabaseUtil.getCollection("alumnos");

    public static boolean vincularPadreAlumno(ObjectId padreId, ObjectId alumnoId, String parentesco) {
        try {
            // Verificar permisos
            if (!AuthController.canManageParentStudentRelations()) {
                return false;
            }

            // Verificar que el padre tenga rol PADRE_FAMILIA
            Document padreDoc = usuariosCollection.find(Filters.eq("_id", padreId)).first();
            if (padreDoc == null || !Rol.PADRE_FAMILIA.name().equals(padreDoc.getString("rol"))) {
                return false;
            }

            // Verificar que el alumno existe
            Document alumnoDoc = alumnosCollection.find(Filters.eq("_id", alumnoId)).first();
            if (alumnoDoc == null) {
                return false;
            }

            // Verificar que no existe ya la vinculación
            Document existente = collection.find(
                Filters.and(
                    Filters.eq("padreId", padreId),
                    Filters.eq("alumnoId", alumnoId),
                    Filters.eq("activo", true)
                )
            ).first();

            if (existente != null) {
                return false; // Ya existe la vinculación
            }

            // Crear la vinculación
            PadreAlumno padreAlumno = new PadreAlumno(padreId, alumnoId, parentesco);
            Document doc = padreAlumno.toDocument();
            collection.insertOne(doc);
            return true;

        } catch (Exception e) {
            System.err.println("Error al vincular padre-alumno: " + e.getMessage());
            return false;
        }
    }

    public static boolean desvincularPadreAlumno(ObjectId padreId, ObjectId alumnoId) {
        try {
            // Verificar permisos
            if (!AuthController.canManageParentStudentRelations()) {
                return false;
            }
            collection.updateOne(
                Filters.and(
                    Filters.eq("padreId", padreId),
                    Filters.eq("alumnoId", alumnoId)
                ),
                new Document("$set", new Document("activo", false))
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al desvincular padre-alumno: " + e.getMessage());
            return false;
        }
    }

    public static List<Alumno> obtenerAlumnosPorPadre(ObjectId padreId) {
        List<Alumno> alumnos = new ArrayList<>();
        try {
            // Buscar vinculaciones activas del padre
            List<Document> vinculaciones = collection.find(
                Filters.and(
                    Filters.eq("padreId", padreId),
                    Filters.eq("activo", true),
                    Filters.eq("autorizado", true)
                )
            ).into(new ArrayList<>());

            // Obtener los alumnos vinculados
            for (Document vinculacion : vinculaciones) {
                ObjectId alumnoId = vinculacion.getObjectId("alumnoId");
                Document alumnoDoc = alumnosCollection.find(
                    Filters.and(
                        Filters.eq("_id", alumnoId),
                        Filters.eq("activo", true)
                    )
                ).first();
                
                if (alumnoDoc != null) {
                    alumnos.add(Alumno.fromDocument(alumnoDoc));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener alumnos por padre: " + e.getMessage());
        }
        return alumnos;
    }

    public static List<Usuario> obtenerPadresPorAlumno(ObjectId alumnoId) {
        List<Usuario> padres = new ArrayList<>();
        try {
            // Buscar vinculaciones activas del alumno
            List<Document> vinculaciones = collection.find(
                Filters.and(
                    Filters.eq("alumnoId", alumnoId),
                    Filters.eq("activo", true),
                    Filters.eq("autorizado", true)
                )
            ).into(new ArrayList<>());

            // Obtener los padres vinculados
            for (Document vinculacion : vinculaciones) {
                ObjectId padreId = vinculacion.getObjectId("padreId");
                Document padreDoc = usuariosCollection.find(
                    Filters.and(
                        Filters.eq("_id", padreId),
                        Filters.eq("activo", true)
                    )
                ).first();
                
                if (padreDoc != null) {
                    padres.add(Usuario.fromDocument(padreDoc));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener padres por alumno: " + e.getMessage());
        }
        return padres;
    }

    public static boolean puedeAccederAlumno(ObjectId padreId, ObjectId alumnoId) {
        try {
            Document vinculacion = collection.find(
                Filters.and(
                    Filters.eq("padreId", padreId),
                    Filters.eq("alumnoId", alumnoId),
                    Filters.eq("activo", true),
                    Filters.eq("autorizado", true)
                )
            ).first();
            
            return vinculacion != null;
        } catch (Exception e) {
            System.err.println("Error al verificar acceso padre-alumno: " + e.getMessage());
            return false;
        }
    }

    public static List<PadreAlumno> obtenerVinculacionesPorPadre(ObjectId padreId) {
        List<PadreAlumno> vinculaciones = new ArrayList<>();
        try {
            List<Document> docs = collection.find(
                Filters.and(
                    Filters.eq("padreId", padreId),
                    Filters.eq("activo", true)
                )
            ).into(new ArrayList<>());

            for (Document doc : docs) {
                vinculaciones.add(PadreAlumno.fromDocument(doc));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener vinculaciones por padre: " + e.getMessage());
        }
        return vinculaciones;
    }

    public static boolean actualizarAutorizacion(ObjectId padreId, ObjectId alumnoId, boolean autorizado) {
        try {
            collection.updateOne(
                Filters.and(
                    Filters.eq("padreId", padreId),
                    Filters.eq("alumnoId", alumnoId)
                ),
                new Document("$set", new Document("autorizado", autorizado))
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar autorización: " + e.getMessage());
            return false;
        }
    }
}