package com.proyecto.reportes.controller;

import com.proyecto.reportes.entity.ReporteNota;
import com.proyecto.reportes.service.ReporteNotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes/notas")
@Tag(name = "Reportes de Notas", description = "Endpoints para reportes de notas académicas")
public class ReporteNotaController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReporteNotaController.class);
    
    @Autowired
    private ReporteNotaService reporteNotaService;
    
    @GetMapping("/alumno/{alumnoId}")
    @Operation(summary = "Obtener notas de un alumno",
            description = "Retorna todas las notas de un alumno específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notas encontradas")
    })
    public ResponseEntity<List<ReporteNota>> obtenerNotasAlumno(@PathVariable Long alumnoId) {
        log.info("GET: Notas del alumno {}", alumnoId);
        List<ReporteNota> notas = reporteNotaService.obtenerNotasAlumno(alumnoId);
        return ResponseEntity.ok(notas);
    }
    
    @GetMapping("/prueba/{pruebaId}")
    @Operation(summary = "Obtener notas de una prueba",
            description = "Retorna todas las notas de una prueba específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notas encontradas")
    })
    public ResponseEntity<List<ReporteNota>> obtenerNotasPrueba(@PathVariable Long pruebaId) {
        log.info("GET: Notas de la prueba {}", pruebaId);
        List<ReporteNota> notas = reporteNotaService.obtenerNotasPrueba(pruebaId);
        return ResponseEntity.ok(notas);
    }
    
    @GetMapping("/alumno/{alumnoId}/promedio")
    @Operation(summary = "Obtener promedio de notas de un alumno",
            description = "Calcula y retorna el promedio de notas de un alumno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promedio calculado exitosamente")
    })
    public ResponseEntity<Map<String, Object>> obtenerPromedioAlumno(@PathVariable Long alumnoId) {
        log.info("GET: Promedio de notas del alumno {}", alumnoId);
        Double promedio = reporteNotaService.obtenerPromedioAlumno(alumnoId);
        Map<String, Object> response = new HashMap<>();
        response.put("alumno_id", alumnoId);
        response.put("promedio", promedio);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/prueba/{pruebaId}/promedio")
    @Operation(summary = "Obtener promedio de una prueba",
            description = "Calcula y retorna el promedio de notas en una prueba específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promedio calculado exitosamente")
    })
    public ResponseEntity<Map<String, Object>> obtenerPromedioPrueba(@PathVariable Long pruebaId) {
        log.info("GET: Promedio de la prueba {}", pruebaId);
        Double promedio = reporteNotaService.obtenerPromedioPrueba(pruebaId);
        Map<String, Object> response = new HashMap<>();
        response.put("prueba_id", pruebaId);
        response.put("promedio", promedio);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/alumno/{alumnoId}/ordenadas")
    @Operation(summary = "Obtener notas de un alumno ordenadas",
            description = "Retorna las notas de un alumno ordenadas de mayor a menor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notas encontradas y ordenadas")
    })
    public ResponseEntity<List<ReporteNota>> obtenerNotasAlumnoOrdenadas(@PathVariable Long alumnoId) {
        log.info("GET: Notas ordenadas del alumno {}", alumnoId);
        List<ReporteNota> notas = reporteNotaService.obtenerNotasAlumnoOrdenadas(alumnoId);
        return ResponseEntity.ok(notas);
    }
}
