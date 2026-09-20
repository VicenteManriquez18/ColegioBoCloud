package com.proyecto.reportes.repository;

import com.proyecto.reportes.entity.ReporteComportamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReporteComportamientoRepository extends JpaRepository<ReporteComportamiento, Long> {
    List<ReporteComportamiento> findByAlumnoId(Long alumnoId);
}
