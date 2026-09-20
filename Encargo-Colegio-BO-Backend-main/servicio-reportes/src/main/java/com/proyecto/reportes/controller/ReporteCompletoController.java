package com.proyecto.reportes.controller;

import com.proyecto.reportes.dto.ReporteCompletoDTO;
import com.proyecto.reportes.service.ReporteCompletoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes/completo")
@Tag(name = "Reportes Completos", description = "Endpoints para la obtención de reportes consolidados (notas, asistencia y comportamiento)")
public class ReporteCompletoController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReporteCompletoController.class);

    private static final String ERROR_KEY = "error";

    @Autowired
    private ReporteCompletoService reporteCompletoService;

    @Autowired
    private org.springframework.web.reactive.function.client.WebClient webClient;

    @org.springframework.beans.factory.annotation.Value("${servicio.matricula.url}")
    private String matriculaUrl;

    private boolean verificarVinculacionApoderado(Long alumnoId, Long apoderadoUsuarioId) {
        try {
            String url = matriculaUrl + "/api/matricula/estudiantes/usuario/" + alumnoId;
            java.util.Map<String, Object> response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {})
                .block();
            if (response != null && response.containsKey("apoderado")) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> apoderado = (java.util.Map<String, Object>) response.get("apoderado");
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

    @GetMapping("/alumno/{alumnoId}")
    @Operation(summary = "Obtener reporte consolidado de un alumno",
            description = "Realiza peticiones a los servicios de académico y asistencia vía WebClient, y los consolida junto con el comportamiento registrado localmente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte consolidado obtenido con éxito")
    })
    public ResponseEntity<Object> obtenerReporteCompleto(
            @PathVariable Long alumnoId,
            @RequestParam(value = "comportamiento", required = false, defaultValue = "true") boolean incluirComportamiento,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        java.util.Map<String, Object> claims = com.proyecto.reportes.config.JwtUtil.parseToken(token);
        if (claims == null || claims.get("rol") == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(java.util.Map.of(ERROR_KEY, "No autorizado"));
        }
        
        String rol = (String) claims.get("rol");
        Long requesterUserId = (Long) claims.get("id");
        
        if ("Alumno".equalsIgnoreCase(rol)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(java.util.Map.of(ERROR_KEY, "Los alumnos no pueden ver reportes"));
        }
        
        if ("Apoderado".equalsIgnoreCase(rol)) {
            if (!verificarVinculacionApoderado(alumnoId, requesterUserId)) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(java.util.Map.of(ERROR_KEY, "Acceso denegado: este alumno no está vinculado a su cuenta de apoderado"));
            }
        }

        log.info("GET: Generando reporte consolidado completo para el alumno {} (incluir comportamiento: {})", alumnoId, incluirComportamiento);
        ReporteCompletoDTO reporte = reporteCompletoService.obtenerReporteCompleto(alumnoId, incluirComportamiento);
        return ResponseEntity.ok(reporte);
    }
}
