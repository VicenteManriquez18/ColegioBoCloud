package com.proyecto.reportes.service;

import com.proyecto.reportes.dto.ReporteCompletoDTO;
import com.proyecto.reportes.entity.ReporteComportamiento;
import com.proyecto.reportes.repository.ReporteComportamientoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ReporteCompletoService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReporteCompletoService.class);

    @Autowired
    private WebClient webClient;

    @Autowired
    private ReporteComportamientoRepository comportamientoRepository;

    @Value("${servicio.academico.url}")
    private String academicoUrl;

    @Value("${servicio.asistencia.url}")
    private String asistenciaUrl;

    public ReporteCompletoDTO obtenerReporteCompleto(Long alumnoId) {
        return obtenerReporteCompleto(alumnoId, true);
    }

    public ReporteCompletoDTO obtenerReporteCompleto(Long alumnoId, boolean incluirComportamiento) {
        log.info("Generando reporte completo para alumno: {} (incluir comportamiento: {})", alumnoId, incluirComportamiento);

        // 1. Obtener Notas desde el servicio-academico
        List<Map<String, Object>> notas = Collections.emptyList();
        try {
            String url = academicoUrl + "/api/academico/notas/alumno/" + alumnoId;
            log.info("Consultando notas en: {}", url);
            notas = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (Exception e) {
            log.error("Error al obtener notas desde servicio-academico para alumno {}: {}", alumnoId, e.getMessage());
        }

        // 2. Obtener Asistencias desde el servicio-asistencia
        List<Map<String, Object>> asistencias = Collections.emptyList();
        try {
            String url = asistenciaUrl + "/api/asistencia/alumno/" + alumnoId;
            log.info("Consultando asistencias en: {}", url);
            asistencias = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (Exception e) {
            log.error("Error al obtener asistencias desde servicio-asistencia para alumno {}: {}", alumnoId, e.getMessage());
        }

        // 3. Obtener Comportamiento desde la base de datos local si está habilitado
        List<ReporteComportamiento> comportamientos = Collections.emptyList();
        if (incluirComportamiento) {
            try {
                comportamientos = comportamientoRepository.findByAlumnoId(alumnoId);
            } catch (Exception e) {
                log.error("Error al obtener comportamiento local para el alumno {}: {}", alumnoId, e.getMessage());
            }
        }

        // 4. Calcular promedio general de notas
        Double promedioGeneral = 0.0;
        if (notas != null && !notas.isEmpty()) {
            double suma = 0.0;
            int count = 0;
            for (Map<String, Object> nota : notas) {
                if (nota.get("valor") != null) {
                    try {
                        suma += Double.parseDouble(nota.get("valor").toString());
                        count++;
                    } catch (NumberFormatException e) {
                        log.warn("Formato inválido de nota para alumno {}: {}", alumnoId, nota.get("valor"));
                    }
                }
            }
            if (count > 0) {
                promedioGeneral = suma / count;
                // Redondear a 2 decimales
                promedioGeneral = Math.round(promedioGeneral * 100.0) / 100.0;
            }
        }

        // 5. Porcentaje de asistencia (dado que no hay base de total de clases programadas en la tabla de asistencia,
        // retornamos la cantidad total de marcas de asistencia)
        Double totalAsistencias = (asistencias != null) ? (double) asistencias.size() : 0.0;

        return new ReporteCompletoDTO(alumnoId, notas, asistencias, comportamientos, promedioGeneral, totalAsistencias);
    }
}
