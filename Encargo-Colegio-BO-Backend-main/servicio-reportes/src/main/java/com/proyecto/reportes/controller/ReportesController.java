package com.proyecto.reportes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes General", description = "Endpoints generales del servicio de reportes")
public class ReportesController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReportesController.class);
    
    @GetMapping("/health")
    @Operation(summary = "Verificar salud del servicio",
            description = "Retorna el estado de salud del servicio de reportes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio funcionando correctamente")
    })
    public ResponseEntity<Map<String, Object>> health() {
        log.info("Health check del servicio de reportes");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Servicio de Reportes");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/info")
    @Operation(summary = "Información del servicio",
            description = "Retorna información sobre el servicio de reportes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información recuperada exitosamente")
    })
    public ResponseEntity<Map<String, Object>> info() {
        log.info("Info del servicio de reportes");
        Map<String, Object> response = new HashMap<>();
        response.put("service_name", "Servicio de Reportes");
        response.put("version", "1.0.0");
        response.put("port", 8085);
        response.put("description", "Servicio de reportes que consume eventos de otros microservicios");
        response.put("event_consumers", new String[]{
                "Eventos de Matrícula",
                "Eventos de Notas",
                "Eventos de Asistencia"
        });
        response.put("available_reports", new String[]{
                "/api/reportes/matriculas",
                "/api/reportes/notas",
                "/api/reportes/asistencias"
        });
        return ResponseEntity.ok(response);
    }
}
