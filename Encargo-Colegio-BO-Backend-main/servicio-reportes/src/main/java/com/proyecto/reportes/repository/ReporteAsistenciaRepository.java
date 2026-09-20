package com.proyecto.reportes.repository;

import com.proyecto.reportes.entity.ReporteAsistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteAsistenciaRepository extends JpaRepository<ReporteAsistencia, Long> {
    
    List<ReporteAsistencia> findByAlumnoId(Long alumnoId);
    
    List<ReporteAsistencia> findByCursoId(Long cursoId);
    
    @Query("SELECT COUNT(r) FROM ReporteAsistencia r WHERE r.alumnoId = :alumnoId AND r.presente = true")
    Long countAsistenciasAlumno(@Param("alumnoId") Long alumnoId);
    
    @Query("SELECT COUNT(r) FROM ReporteAsistencia r WHERE r.alumnoId = :alumnoId AND r.presente = false")
    Long countInasistenciasAlumno(@Param("alumnoId") Long alumnoId);
    
    @Query("SELECT (COUNT(r) * 100.0 / (SELECT COUNT(*) FROM ReporteAsistencia WHERE alumnoId = :alumnoId)) " +
           "FROM ReporteAsistencia r WHERE r.alumnoId = :alumnoId AND r.presente = true")
    Double porcentajeAsistenciaAlumno(@Param("alumnoId") Long alumnoId);
}
