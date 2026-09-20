package com.proyecto.ColegioBackend.repository;

import com.proyecto.ColegioBackend.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {
    List<Nota> findByPruebaId(Long pruebaId);
    List<Nota> findByAlumnoId(Long alumnoId);
    Optional<Nota> findByPruebaIdAndAlumnoId(Long pruebaId, Long alumnoId);
}
