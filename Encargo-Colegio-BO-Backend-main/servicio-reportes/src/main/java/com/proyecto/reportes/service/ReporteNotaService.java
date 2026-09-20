package com.proyecto.reportes.service;

import com.proyecto.reportes.entity.ReporteNota;
import com.proyecto.reportes.repository.ReporteNotaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteNotaService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReporteNotaService.class);
    
    @Autowired
    private ReporteNotaRepository reporteNotaRepository;
    
    public List<ReporteNota> obtenerNotasAlumno(Long alumnoId) {
        log.info("Obteniendo notas para alumno: {}", alumnoId);
        return reporteNotaRepository.findByAlumnoId(alumnoId);
    }
    
    public List<ReporteNota> obtenerNotasPrueba(Long pruebaId) {
        log.info("Obteniendo notas para prueba: {}", pruebaId);
        return reporteNotaRepository.findByPruebaId(pruebaId);
    }
    
    public Double obtenerPromedioAlumno(Long alumnoId) {
        log.info("Calculando promedio para alumno: {}", alumnoId);
        return reporteNotaRepository.promedioNotasPorAlumno(alumnoId);
    }
    
    public Double obtenerPromedioPrueba(Long pruebaId) {
        log.info("Calculando promedio para prueba: {}", pruebaId);
        return reporteNotaRepository.promedioNotasPorPrueba(pruebaId);
    }
    
    public List<ReporteNota> obtenerNotasAlumnoOrdenadas(Long alumnoId) {
        log.info("Obteniendo notas ordenadas para alumno: {}", alumnoId);
        return reporteNotaRepository.notasAlumnoOrdenado(alumnoId);
    }
}
