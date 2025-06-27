// GrupoController.java
package com.controlescolar.controllers;

import com.controlescolar.models.Grupo;
import com.controlescolar.models.Alumno;
import com.controlescolar.models.Materia;
import com.controlescolar.models.Profesor;
import com.controlescolar.utils.DatabaseUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

public class GrupoController {
    private static MongoCollection<Document> gruposCollection = DatabaseUtil.getCollection("grupos");
    private static MongoCollection<Document> alumnosCollection = DatabaseUtil.getCollection("alumnos");
    private static MongoCollection<Document> materiasCollection = DatabaseUtil.getCollection("materias");
    private static MongoCollection<Document> profesoresCollection = DatabaseUtil.getCollection("profesores");

    public static boolean crearGrupo(Grupo grupo) {
        try {
            // Verificar si el código ya existe
            if (gruposCollection.find(Filters.eq("codigo", grupo.getCodigo())).first() != null) {
                return false;
            }

            gruposCollection.insertOne(grupo.toDocument());
            return true;
        } catch (Exception e) {
            System.err.println("Error al crear grupo: " + e.getMessage());
            return false;
        }
    }

    public static boolean actualizarGrupo(Grupo grupo) {
        try {
            Document updateDoc = new Document()
                    .append("nombre", grupo.getNombre())
                    .append("grado", grupo.getGrado())
                    .append("seccion", grupo.getSeccion())
                    .append("profesorTitularId", grupo.getProfesorTitularId())
                    .append("activo", grupo.isActivo());

            gruposCollection.updateOne(
                    Filters.eq("_id", grupo.getId()),
                    new Document("$set", updateDoc)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar grupo: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarGrupo(ObjectId grupoId) {
        try {
            // Marcar como inactivo en lugar de eliminar
            gruposCollection.updateOne(
                    Filters.eq("_id", grupoId),
                    Updates.set("activo", false)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar grupo: " + e.getMessage());
            return false;
        }
    }

    public static List<Grupo> obtenerTodosLosGrupos() {
        List<Grupo> grupos = new ArrayList<>();
        try {
            gruposCollection.find(Filters.eq("activo", true))
                    .forEach(doc -> grupos.add(Grupo.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener grupos: " + e.getMessage());
        }
        return grupos;
    }

    public static Grupo obtenerGrupoPorId(ObjectId grupoId) {
        try {
            Document doc = gruposCollection.find(Filters.eq("_id", grupoId)).first();
            return doc != null ? Grupo.fromDocument(doc) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener grupo por ID: " + e.getMessage());
            return null;
        }
    }

    public static List<Grupo> obtenerGruposPorGrado(String grado) {
        List<Grupo> grupos = new ArrayList<>();
        try {
            gruposCollection.find(
                    Filters.and(
                            Filters.eq("grado", grado),
                            Filters.eq("activo", true)
                    )
            ).forEach(doc -> grupos.add(Grupo.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener grupos por grado: " + e.getMessage());
        }
        return grupos;
    }

    // Gestión de alumnos en grupos
    public static boolean asignarAlumnoAGrupo(ObjectId alumnoId, ObjectId grupoId) {
        try {
            // Agregar alumno al grupo
            gruposCollection.updateOne(
                    Filters.eq("_id", grupoId),
                    Updates.addToSet("alumnosIds", alumnoId)
            );

            // Agregar grupo al alumno
            alumnosCollection.updateOne(
                    Filters.eq("_id", alumnoId),
                    Updates.addToSet("gruposIds", grupoId)
            );

            return true;
        } catch (Exception e) {
            System.err.println("Error al asignar alumno a grupo: " + e.getMessage());
            return false;
        }
    }

    public static boolean removerAlumnoDeGrupo(ObjectId alumnoId, ObjectId grupoId) {
        try {
            // Remover alumno del grupo
            gruposCollection.updateOne(
                    Filters.eq("_id", grupoId),
                    Updates.pull("alumnosIds", alumnoId)
            );

            // Remover grupo del alumno
            alumnosCollection.updateOne(
                    Filters.eq("_id", alumnoId),
                    Updates.pull("gruposIds", grupoId)
            );

            return true;
        } catch (Exception e) {
            System.err.println("Error al remover alumno de grupo: " + e.getMessage());
            return false;
        }
    }

    public static List<Alumno> obtenerAlumnosDeGrupo(ObjectId grupoId) {
        List<Alumno> alumnos = new ArrayList<>();
        try {
            Grupo grupo = obtenerGrupoPorId(grupoId);
            if (grupo != null && grupo.getAlumnosIds() != null) {
                for (ObjectId alumnoId : grupo.getAlumnosIds()) {
                    Document doc = alumnosCollection.find(Filters.eq("_id", alumnoId)).first();
                    if (doc != null) {
                        alumnos.add(Alumno.fromDocument(doc));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener alumnos del grupo: " + e.getMessage());
        }
        return alumnos;
    }

    public static List<Grupo> obtenerGruposDeAlumno(ObjectId alumnoId) {
        List<Grupo> grupos = new ArrayList<>();
        try {
            Alumno alumno = AlumnoController.obtenerAlumnoPorId(alumnoId);
            if (alumno != null && alumno.getGruposIds() != null) {
                for (ObjectId grupoId : alumno.getGruposIds()) {
                    Grupo grupo = obtenerGrupoPorId(grupoId);
                    if (grupo != null) {
                        grupos.add(grupo);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener grupos del alumno: " + e.getMessage());
        }
        return grupos;
    }

    // Gestión de materias en grupos
    public static boolean asignarMateriaAGrupo(ObjectId materiaId, ObjectId grupoId) {
        try {
            gruposCollection.updateOne(
                    Filters.eq("_id", grupoId),
                    Updates.addToSet("materiasIds", materiaId)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al asignar materia a grupo: " + e.getMessage());
            return false;
        }
    }

    public static boolean removerMateriaDeGrupo(ObjectId materiaId, ObjectId grupoId) {
        try {
            gruposCollection.updateOne(
                    Filters.eq("_id", grupoId),
                    Updates.pull("materiasIds", materiaId)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al remover materia de grupo: " + e.getMessage());
            return false;
        }
    }

    public static List<Materia> obtenerMateriasDeGrupo(ObjectId grupoId) {
        List<Materia> materias = new ArrayList<>();
        try {
            Grupo grupo = obtenerGrupoPorId(grupoId);
            if (grupo != null && grupo.getMateriasIds() != null) {
                for (ObjectId materiaId : grupo.getMateriasIds()) {
                    Document doc = materiasCollection.find(Filters.eq("_id", materiaId)).first();
                    if (doc != null) {
                        materias.add(Materia.fromDocument(doc));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener materias del grupo: " + e.getMessage());
        }
        return materias;
    }

    // Gestión de profesor titular
    public static boolean asignarProfesorTitular(ObjectId profesorId, ObjectId grupoId) {
        try {
            gruposCollection.updateOne(
                    Filters.eq("_id", grupoId),
                    Updates.set("profesorTitularId", profesorId)
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error al asignar profesor titular: " + e.getMessage());
            return false;
        }
    }

    public static Profesor obtenerProfesorTitular(ObjectId grupoId) {
        try {
            Grupo grupo = obtenerGrupoPorId(grupoId);
            if (grupo != null && grupo.getProfesorTitularId() != null) {
                Document doc = profesoresCollection.find(Filters.eq("_id", grupo.getProfesorTitularId())).first();
                return doc != null ? Profesor.fromDocument(doc) : null;
            }
        } catch (Exception e) {
            System.err.println("Error al obtener profesor titular: " + e.getMessage());
        }
        return null;
    }

    public static List<Grupo> obtenerGruposPorProfesor(ObjectId profesorId) {
        List<Grupo> grupos = new ArrayList<>();
        try {
            gruposCollection.find(
                    Filters.and(
                            Filters.eq("profesorTitularId", profesorId),
                            Filters.eq("activo", true)
                    )
            ).forEach(doc -> grupos.add(Grupo.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al obtener grupos por profesor: " + e.getMessage());
        }
        return grupos;
    }

    // Métodos de utilidad
    public static int contarAlumnosEnGrupo(ObjectId grupoId) {
        try {
            List<Alumno> alumnos = obtenerAlumnosDeGrupo(grupoId);
            return alumnos.size();
        } catch (Exception e) {
            System.err.println("Error al contar alumnos en grupo: " + e.getMessage());
            return 0;
        }
    }

    public static boolean validarCapacidadGrupo(ObjectId grupoId, int maxCapacidad) {
        return contarAlumnosEnGrupo(grupoId) < maxCapacidad;
    }

    public static List<Alumno> obtenerAlumnosDisponiblesParaGrupo(ObjectId grupoId) {
        List<Alumno> alumnosDisponibles = new ArrayList<>();
        List<Alumno> todosAlumnos = AlumnoController.obtenerAlumnos();
        List<Alumno> alumnosDelGrupo = obtenerAlumnosDeGrupo(grupoId);

        for (Alumno alumno : todosAlumnos) {
            boolean yaEstaEnGrupo = alumnosDelGrupo.stream()
                    .anyMatch(a -> a.getId().equals(alumno.getId()));
            if (!yaEstaEnGrupo) {
                alumnosDisponibles.add(alumno);
            }
        }

        return alumnosDisponibles;
    }

    // Métodos adicionales para la vista de grupos
    public static List<Grupo> buscarGrupos(String termino) {
        List<Grupo> grupos = new ArrayList<>();
        try {
            Document regex = new Document("$regex", termino).append("$options", "i");
            Document filter = new Document("$or", List.of(
                    new Document("codigo", regex),
                    new Document("nombre", regex),
                    new Document("grado", regex),
                    new Document("seccion", regex)
            )).append("activo", true);

            gruposCollection.find(filter).forEach(doc -> grupos.add(Grupo.fromDocument(doc)));
        } catch (Exception e) {
            System.err.println("Error al buscar grupos: " + e.getMessage());
        }
        return grupos;
    }

    public static boolean asignarAlumnosAGrupo(ObjectId grupoId, List<ObjectId> alumnosIds) {
        try {
            // Actualizar las asignaciones del grupo
            gruposCollection.updateOne(
                    Filters.eq("_id", grupoId),
                    Updates.set("alumnosIds", alumnosIds)
            );

            // Actualizar las referencias en los alumnos
            // Obtener todos los alumnos que actualmente tienen este grupo
            List<Document> alumnosConGrupo = new ArrayList<>();
            alumnosCollection.find(Filters.in("gruposIds", grupoId))
                    .forEach(alumnosConGrupo::add);

            // Remover este grupo de todos los alumnos que lo tienen
            for (Document alumnoDoc : alumnosConGrupo) {
                ObjectId alumnoId = alumnoDoc.getObjectId("_id");
                try {
                    alumnosCollection.updateOne(
                            Filters.eq("_id", alumnoId),
                            Updates.pull("gruposIds", grupoId)
                    );
                } catch (Exception e) {
                    // Si falla el pull, inicializar el campo como array vacío
                    alumnosCollection.updateOne(
                            Filters.eq("_id", alumnoId),
                            Updates.set("gruposIds", new ArrayList<ObjectId>())
                    );
                }
            }

            // Agregar este grupo a los alumnos seleccionados
            for (ObjectId alumnoId : alumnosIds) {
                try {
                    alumnosCollection.updateOne(
                            Filters.eq("_id", alumnoId),
                            Updates.addToSet("gruposIds", grupoId)
                    );
                } catch (Exception e) {
                    // Si falla addToSet, inicializar el campo y luego agregar
                    alumnosCollection.updateOne(
                            Filters.eq("_id", alumnoId),
                            Updates.set("gruposIds", List.of(grupoId))
                    );
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error al asignar alumnos a grupo: " + e.getMessage());
            return false;
        }
    }

    public static boolean asignarMateriasAGrupo(ObjectId grupoId, List<ObjectId> materiasIds) {
        try {
            // Actualizar las materias asignadas al grupo
            gruposCollection.updateOne(
                    Filters.eq("_id", grupoId),
                    Updates.set("materiasIds", materiasIds)
            );

            return true;
        } catch (Exception e) {
            System.err.println("Error al asignar materias a grupo: " + e.getMessage());
            return false;
        }
    }
}