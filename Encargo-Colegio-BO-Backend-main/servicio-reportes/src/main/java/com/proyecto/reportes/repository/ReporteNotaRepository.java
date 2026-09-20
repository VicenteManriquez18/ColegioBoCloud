package com.proyecto.reportes.repository;

import com.proyecto.reportes.entity.ReporteNota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteNotaRepository extends JpaRepository<ReporteNota, Long> {
    
    List<ReporteNota> findByAlumnoId(Long alumnoId);
    
    List<ReporteNota> findByPruebaId(Long pruebaId);
    
    @Query("SELECT AVG(r.valor) FROM ReporteNota r WHERE r.alumnoId = :alumnoId")
    Double promedioNotasPorAlumno(@Param("alumnoId") Long alumnoId);
    
    @Query("SELECT AVG(r.valor) FROM ReporteNota r WHERE r.pruebaId = :pruebaId")
    Double promedioNotasPorPrueba(@Param("pruebaId") Long pruebaId);
    
    @Query("SELECT r FROM ReporteNota r WHERE r.alumnoId = :alumnoId ORDER BY r.valor DESC")
    List<ReporteNota> notasAlumnoOrdenado(@Param("alumnoId") Long alumnoId);
}
