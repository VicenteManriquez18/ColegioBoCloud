package com.proyecto.reportes.service;

import com.proyecto.reportes.entity.ReporteAsistencia;
import com.proyecto.reportes.repository.ReporteAsistenciaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteAsistenciaService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReporteAsistenciaService.class);
    
    @Autowired
    private ReporteAsistenciaRepository reporteAsistenciaRepository;
    
    public List<ReporteAsistencia> obtenerAsistenciasAlumno(Long alumnoId) {
        log.info("Obteniendo asistencias para alumno: {}", alumnoId);
        return reporteAsistenciaRepository.findByAlumnoId(alumnoId);
    }
    
    public List<ReporteAsistencia> obtenerAsistenciasCurso(Long cursoId) {
        log.info("Obteniendo asistencias para curso: {}", cursoId);
        return reporteAsistenciaRepository.findByCursoId(cursoId);
    }
    
    public Long obtenerTotalAsistenciasAlumno(Long alumnoId) {
        log.info("Contando asistencias para alumno: {}", alumnoId);
        return reporteAsistenciaRepository.countAsistenciasAlumno(alumnoId);
    }
    
    public Long obtenerTotalInasistenciasAlumno(Long alumnoId) {
        log.info("Contando inasistencias para alumno: {}", alumnoId);
        return reporteAsistenciaRepository.countInasistenciasAlumno(alumnoId);
    }
    
    public Double obtenerPorcentajeAsistenciaAlumno(Long alumnoId) {
        log.info("Calculando porcentaje de asistencia para alumno: {}", alumnoId);
        return reporteAsistenciaRepository.porcentajeAsistenciaAlumno(alumnoId);
    }
}
