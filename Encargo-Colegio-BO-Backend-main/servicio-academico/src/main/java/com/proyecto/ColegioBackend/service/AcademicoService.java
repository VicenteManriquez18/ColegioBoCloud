package com.proyecto.ColegioBackend.service;

import com.proyecto.ColegioBackend.model.Curso;
import com.proyecto.ColegioBackend.model.Matricula;
import com.proyecto.ColegioBackend.model.Prueba;
import com.proyecto.ColegioBackend.model.Nota;

import java.util.List;

public interface AcademicoService {
    // Cursos
    List<Curso> listarCursos();
    Curso obtenerCursoPorId(Long id);
    Curso crearCurso(Curso curso);
    Curso asignarProfesor(Long cursoId, Long profesorId);
    Curso obtenerCursoPorNombre(String nombre);
    
    // Asignaturas de Curso
    java.util.List<com.proyecto.ColegioBackend.model.CursoAsignatura> listarAsignacionesPorCurso(Long cursoId);
    com.proyecto.ColegioBackend.model.CursoAsignatura asignarProfesorAsignatura(Long cursoId, String asignatura, Long profesorId);
    java.util.List<com.proyecto.ColegioBackend.model.CursoAsignatura> listarTodasAsignaciones();
    java.util.List<com.proyecto.ColegioBackend.model.CursoAsignatura> listarAsignacionesPorProfesor(Long profesorId);
    com.proyecto.ColegioBackend.model.CursoAsignatura obtenerAsignacionPorCursoYAsignatura(Long cursoId, String asignatura);

    // Matrículas
    Matricula matricularAlumno(Long usuarioId, Long cursoId);
    List<Matricula> listarMatriculasPorCurso(Long cursoId);
    List<Matricula> listarMatriculasPorAlumno(Long usuarioId);
    void eliminarMatriculasPorAlumno(Long usuarioId);

    // Pruebas
    Prueba crearPrueba(Long cursoId, Prueba prueba);
    List<Prueba> listarPruebasPorCurso(Long cursoId);
    Prueba obtenerPruebaPorId(Long id);

    // Notas
    Nota registrarNota(Long pruebaId, Long alumnoId, Double valor, String comentario);
    List<Nota> listarNotasPorPrueba(Long pruebaId);
    List<Nota> listarNotasPorAlumno(Long alumnoId);
}
