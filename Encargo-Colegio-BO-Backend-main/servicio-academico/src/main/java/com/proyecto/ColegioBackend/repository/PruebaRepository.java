package com.proyecto.ColegioBackend.repository;

import com.proyecto.ColegioBackend.model.Prueba;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PruebaRepository extends JpaRepository<Prueba, Long> {
    List<Prueba> findByCursoId(Long cursoId);
}
