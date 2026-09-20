package com.proyecto.ColegioBackend.repository;

import com.proyecto.ColegioBackend.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {
    Optional<Curso> findByNombre(String nombre);
    Optional<Curso> findByCodigo(String codigo);
    Optional<Curso> findByProfesorId(Long profesorId);
}
