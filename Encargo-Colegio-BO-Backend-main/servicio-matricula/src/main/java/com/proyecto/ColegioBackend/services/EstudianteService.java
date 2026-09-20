package com.proyecto.ColegioBackend.services;

import java.util.List;

import com.proyecto.ColegioBackend.model.Estudiante;

public interface EstudianteService{
    List<Estudiante> listarTodosEstudiantes();
    Estudiante registrarMatriculaCompleta(Estudiante estudiante);
    Estudiante buscarPorId(Long id);
    Estudiante buscarEstudiantePorRut(String rut);
}
