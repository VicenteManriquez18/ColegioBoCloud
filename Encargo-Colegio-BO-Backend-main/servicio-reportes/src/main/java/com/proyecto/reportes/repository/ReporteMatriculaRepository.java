package com.proyecto.reportes.repository;

import com.proyecto.reportes.entity.ReporteMatricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteMatriculaRepository extends JpaRepository<ReporteMatricula, Long> {
    
    List<ReporteMatricula> findByAlumnoId(Long alumnoId);
    
    List<ReporteMatricula> findByCursoId(Long cursoId);
    
    @Query("SELECT r FROM ReporteMatricula r WHERE r.alumnoId = :alumnoId AND r.cursoId = :cursoId")
    List<ReporteMatricula> findByAlumnoAndCurso(@Param("alumnoId") Long alumnoId, @Param("cursoId") Long cursoId);
    
    @Query("SELECT COUNT(r) FROM ReporteMatricula r WHERE r.cursoId = :cursoId AND r.estado = 'ACTIVO'")
    Long countAlumnosActivosPorCurso(@Param("cursoId") Long cursoId);
}
