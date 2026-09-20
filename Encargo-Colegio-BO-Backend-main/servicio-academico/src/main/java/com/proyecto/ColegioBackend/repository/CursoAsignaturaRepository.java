package com.proyecto.ColegioBackend.repository;

import com.proyecto.ColegioBackend.model.CursoAsignatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CursoAsignaturaRepository extends JpaRepository<CursoAsignatura, Long> {
    List<CursoAsignatura> findByCursoId(Long cursoId);
    Optional<CursoAsignatura> findByCursoIdAndAsignaturaIgnoreCase(Long cursoId, String asignatura);
    List<CursoAsignatura> findByProfesorId(Long profesorId);
}
