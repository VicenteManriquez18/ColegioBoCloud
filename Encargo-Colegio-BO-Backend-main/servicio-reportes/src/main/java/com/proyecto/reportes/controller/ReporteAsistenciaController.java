package com.proyecto.reportes.controller;

import com.proyecto.reportes.entity.ReporteAsistencia;
import com.proyecto.reportes.service.ReporteAsistenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes/asistencias")
@Tag(name = "Reportes de Asistencia", description = "Endpoints para reportes de asistencias")
public class ReporteAsistenciaController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReporteAsistenciaController.class);
    
    private static final String ALUMNO_ID_KEY = "alumno_id";

    @Autowired
    private ReporteAsistenciaService reporteAsistenciaService;
    
    @GetMapping("/alumno/{alumnoId}")
    @Operation(summary = "Obtener asistencias de un alumno",
            description = "Retorna todas las asistencias registradas de un alumno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asistencias encontradas")
    })
    public ResponseEntity<List<ReporteAsistencia>> obtenerAsistenciasAlumno(@PathVariable Long alumnoId) {
        log.info("GET: Asistencias del alumno {}", alumnoId);
        List<ReporteAsistencia> asistencias = reporteAsistenciaService.obtenerAsistenciasAlumno(alumnoId);
        return ResponseEntity.ok(asistencias);
    }
    
    @GetMapping("/curso/{cursoId}")
    @Operation(summary = "Obtener asistencias de un curso",
            description = "Retorna todas las asistencias registradas en un curso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asistencias encontradas")
    })
    public ResponseEntity<List<ReporteAsistencia>> obtenerAsistenciasCurso(@PathVariable Long cursoId) {
        log.info("GET: Asistencias del curso {}", cursoId);
        List<ReporteAsistencia> asistencias = reporteAsistenciaService.obtenerAsistenciasCurso(cursoId);
        return ResponseEntity.ok(asistencias);
    }
    
    @GetMapping("/alumno/{alumnoId}/total-asistencias")
    @Operation(summary = "Contar asistencias de un alumno",
            description = "Retorna el número total de asistencias de un alumno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conteo realizado exitosamente")
    })
    public ResponseEntity<Map<String, Object>> obtenerTotalAsistencias(@PathVariable Long alumnoId) {
        log.info("GET: Total de asistencias del alumno {}", alumnoId);
        Long total = reporteAsistenciaService.obtenerTotalAsistenciasAlumno(alumnoId);
        Map<String, Object> response = new HashMap<>();
        response.put(ALUMNO_ID_KEY, alumnoId);
        response.put("total_asistencias", total);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/alumno/{alumnoId}/total-inasistencias")
    @Operation(summary = "Contar inasistencias de un alumno",
            description = "Retorna el número total de inasistencias de un alumno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conteo realizado exitosamente")
    })
    public ResponseEntity<Map<String, Object>> obtenerTotalInasistencias(@PathVariable Long alumnoId) {
        log.info("GET: Total de inasistencias del alumno {}", alumnoId);
        Long total = reporteAsistenciaService.obtenerTotalInasistenciasAlumno(alumnoId);
        Map<String, Object> response = new HashMap<>();
        response.put(ALUMNO_ID_KEY, alumnoId);
        response.put("total_inasistencias", total);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/alumno/{alumnoId}/porcentaje-asistencia")
    @Operation(summary = "Obtener porcentaje de asistencia de un alumno",
            description = "Calcula y retorna el porcentaje de asistencia de un alumno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Porcentaje calculado exitosamente")
    })
    public ResponseEntity<Map<String, Object>> obtenerPorcentajeAsistencia(@PathVariable Long alumnoId) {
        log.info("GET: Porcentaje de asistencia del alumno {}", alumnoId);
        Double porcentaje = reporteAsistenciaService.obtenerPorcentajeAsistenciaAlumno(alumnoId);
        Map<String, Object> response = new HashMap<>();
        response.put(ALUMNO_ID_KEY, alumnoId);
        response.put("porcentaje_asistencia", porcentaje);
        return ResponseEntity.ok(response);
    }
}
