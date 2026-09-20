package com.proyecto.ColegioBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.ColegioBackend.model.Apoderado;
import java.util.Optional;

public interface ApoderadoRepository extends JpaRepository<Apoderado, Long> {
    Optional<Apoderado> findByRut(String rut);
    Optional<Apoderado> findByUsuarioId(Long usuarioId);
}