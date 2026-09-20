package com.proyecto.reportes.service;

import com.proyecto.reportes.entity.ReporteMatricula;
import com.proyecto.reportes.repository.ReporteMatriculaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteMatriculaService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReporteMatriculaService.class);
    
    @Autowired
    private ReporteMatriculaRepository reporteMatriculaRepository;
    
    public List<ReporteMatricula> obtenerReportesAlumno(Long alumnoId) {
        log.info("Obteniendo reportes de matrícula para alumno: {}", alumnoId);
        return reporteMatriculaRepository.findByAlumnoId(alumnoId);
    }
    
    public List<ReporteMatricula> obtenerReportesCurso(Long cursoId) {
        log.info("Obteniendo reportes de matrícula para curso: {}", cursoId);
        return reporteMatriculaRepository.findByCursoId(cursoId);
    }
    
    public Long contarAlumnosActivosPorCurso(Long cursoId) {
        log.info("Contando alumnos activos en curso: {}", cursoId);
        return reporteMatriculaRepository.countAlumnosActivosPorCurso(cursoId);
    }
    
    public List<ReporteMatricula> obtenerMatriculasAlumnoEnCurso(Long alumnoId, Long cursoId) {
        log.info("Obteniendo matrículas del alumno {} en curso {}", alumnoId, cursoId);
        return reporteMatriculaRepository.findByAlumnoAndCurso(alumnoId, cursoId);
    }
}
