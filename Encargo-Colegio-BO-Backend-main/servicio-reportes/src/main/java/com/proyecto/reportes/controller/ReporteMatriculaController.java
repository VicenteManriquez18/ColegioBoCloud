package com.proyecto.reportes.controller;

import com.proyecto.reportes.entity.ReporteMatricula;
import com.proyecto.reportes.service.ReporteMatriculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes/matriculas")
@Tag(name = "Reportes de Matrícula", description = "Endpoints para reportes de matrículas")
public class ReporteMatriculaController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReporteMatriculaController.class);
    
    @Autowired
    private ReporteMatriculaService reporteMatriculaService;
    
    @GetMapping("/alumno/{alumnoId}")
    @Operation(summary = "Obtener reportes de matrícula de un alumno",
            description = "Retorna todos los reportes de matrícula para un alumno específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reportes encontrados"),
            @ApiResponse(responseCode = "404", description = "Alumno no encontrado")
    })
    public ResponseEntity<List<ReporteMatricula>> obtenerReportesAlumno(@PathVariable Long alumnoId) {
        log.info("GET: Reportes de matrícula del alumno {}", alumnoId);
        List<ReporteMatricula> reportes = reporteMatriculaService.obtenerReportesAlumno(alumnoId);
        return ResponseEntity.ok(reportes);
    }
    
    @GetMapping("/curso/{cursoId}")
    @Operation(summary = "Obtener reportes de matrícula de un curso",
            description = "Retorna todos los reportes de matrícula para un curso específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reportes encontrados")
    })
    public ResponseEntity<List<ReporteMatricula>> obtenerReportesCurso(@PathVariable Long cursoId) {
        log.info("GET: Reportes de matrícula del curso {}", cursoId);
        List<ReporteMatricula> reportes = reporteMatriculaService.obtenerReportesCurso(cursoId);
        return ResponseEntity.ok(reportes);
    }
    
    @GetMapping("/curso/{cursoId}/count-activos")
    @Operation(summary = "Contar alumnos activos en un curso",
            description = "Retorna el número de alumnos activos matriculados en un curso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conteo realizado exitosamente")
    })
    public ResponseEntity<Long> contarAlumnosActivos(@PathVariable Long cursoId) {
        log.info("GET: Contando alumnos activos en curso {}", cursoId);
        Long count = reporteMatriculaService.contarAlumnosActivosPorCurso(cursoId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/alumno/{alumnoId}/curso/{cursoId}")
    @Operation(summary = "Obtener matrículas de un alumno en un curso específico",
            description = "Retorna las matrículas de un alumno en un curso particular")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matrículas encontradas")
    })
    public ResponseEntity<List<ReporteMatricula>> obtenerMatriculasAlumnoEnCurso(
            @PathVariable Long alumnoId, 
            @PathVariable Long cursoId) {
        log.info("GET: Matrículas del alumno {} en curso {}", alumnoId, cursoId);
        List<ReporteMatricula> matriculas = reporteMatriculaService.obtenerMatriculasAlumnoEnCurso(alumnoId, cursoId);
        return ResponseEntity.ok(matriculas);
    }
}
