package com.proyecto.ColegioBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.ColegioBackend.model.Estudiante;

import java.util.List;
import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    List<Estudiante> findByCursoId(Long cursoId);
    Optional<Estudiante> findByRut(String rut);
    Optional<Estudiante> findByUsuarioId(Long usuarioId);
    List<Estudiante> findByApoderadoId(Long apoderadoId);
}
