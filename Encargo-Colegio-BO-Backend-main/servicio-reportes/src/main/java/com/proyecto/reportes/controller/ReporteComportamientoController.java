package com.proyecto.reportes.controller;

import com.proyecto.reportes.entity.ReporteComportamiento;
import com.proyecto.reportes.repository.ReporteComportamientoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes/comportamiento")
@Tag(name = "Reportes de Comportamiento", description = "Endpoints para la gestión de comportamiento y conducta escolar")
public class ReporteComportamientoController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReporteComportamientoController.class);

    @Autowired
    private ReporteComportamientoRepository comportamientoRepository;

    @Autowired
    private org.springframework.web.reactive.function.client.WebClient webClient;

    @org.springframework.beans.factory.annotation.Value("${servicio.matricula.url}")
    private String matriculaUrl;

    private boolean verificarVinculacionApoderado(Long alumnoId, Long apoderadoUsuarioId) {
        try {
            String url = matriculaUrl + "/api/matricula/estudiantes/usuario/" + alumnoId;
            java.util.Map response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(java.util.Map.class)
                .block();
            if (response != null && response.containsKey("apoderado")) {
                java.util.Map apoderado = (java.util.Map) response.get("apoderado");
                if (apoderado != null && apoderado.containsKey("usuarioId")) {
                    Long val = Long.valueOf(apoderado.get("usuarioId").toString());
                    return val.equals(apoderadoUsuarioId);
                }
            }
        } catch (Exception e) {
            log.error("Error al verificar vinculación apoderado para alumnoId {}: {}", alumnoId, e.getMessage());
        }
        return false;
    }

    @PostMapping
    @Operation(summary = "Registrar comportamiento de un alumno",
            description = "Guarda una evaluación conductual para un alumno específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comportamiento registrado correctamente")
    })
    public ResponseEntity<?> registrarComportamiento(
            @RequestBody ReporteComportamiento comportamiento,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        java.util.Map<String, Object> claims = com.proyecto.reportes.config.JwtUtil.parseToken(token);
        if (claims == null || claims.get("rol") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "No autorizado"));
        }
        
        String rol = (String) claims.get("rol");
        if (!"Profesor".equalsIgnoreCase(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("error", "Solo los profesores pueden registrar reportes de comportamiento"));
        }

        log.info("POST: Registrando comportamiento para el alumno {}", comportamiento.getAlumnoId());
        ReporteComportamiento guardado = comportamientoRepository.save(comportamiento);
        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar todos los comportamientos",
            description = "Retorna la lista completa de reportes de comportamiento (Solo Admin)")
    public ResponseEntity<?> listarTodosLosComportamientos(@RequestHeader(value = "Authorization", required = false) String token) {
        java.util.Map<String, Object> claims = com.proyecto.reportes.config.JwtUtil.parseToken(token);
        if (claims == null || claims.get("rol") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "No autorizado"));
        }
        String rol = (String) claims.get("rol");
        if (!"Administrador".equalsIgnoreCase(rol) && !"Admin".equalsIgnoreCase(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("error", "Acceso denegado: solo el administrador puede listar todos los reportes"));
        }
        return ResponseEntity.ok(comportamientoRepository.findAll());
    }

    @GetMapping("/alumno/{alumnoId}")
    @Operation(summary = "Obtener comportamientos de un alumno",
            description = "Retorna el historial de evaluaciones conductuales registradas de un alumno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registros encontrados")
    })
    public ResponseEntity<?> obtenerComportamientosAlumno(
            @PathVariable Long alumnoId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        java.util.Map<String, Object> claims = com.proyecto.reportes.config.JwtUtil.parseToken(token);
        if (claims == null || claims.get("rol") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "No autorizado"));
        }
        
        String rol = (String) claims.get("rol");
        Long requesterUserId = (Long) claims.get("id");
        
        if ("Alumno".equalsIgnoreCase(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("error", "Los alumnos no pueden ver reportes de comportamiento"));
        }
        
        if ("Apoderado".equalsIgnoreCase(rol)) {
            if (!verificarVinculacionApoderado(alumnoId, requesterUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("error", "Acceso denegado: este alumno no está vinculado a su cuenta de apoderado"));
            }
        }

        log.info("GET: Historial de comportamiento del alumno {}", alumnoId);
        List<ReporteComportamiento> lista = comportamientoRepository.findByAlumnoId(alumnoId);
        return ResponseEntity.ok(lista);
    }
}
