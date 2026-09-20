package com.colegio.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
@Tag(name = "Gateway", description = "Endpoints del API Gateway")
public class GatewayController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GatewayController.class);

    @GetMapping("/health")
    @Operation(summary = "Verificar salud del Gateway",
            description = "Retorna el estado de salud del API Gateway")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gateway funcionando correctamente")
    })
    public Mono<Map<String, Object>> getHealth() {
        log.info("Health check del gateway");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "API Gateway");
        response.put("timestamp", System.currentTimeMillis());
        return Mono.just(response);
    }

    @GetMapping("/info")
    @Operation(summary = "Información del Gateway",
            description = "Retorna información sobre los servicios disponibles en el gateway")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información recuperada exitosamente")
    })
    public Mono<Map<String, Object>> getInfo() {
        log.info("Info del gateway");
        Map<String, Object> response = new HashMap<>();
        response.put("gateway_name", "API Gateway - Colegio");
        response.put("version", "1.0.0");
        response.put("port", 9090);
        response.put("services", new String[]{
                "Servicio Usuarios (puerto 8081)",
                "Servicio Asistencia (puerto 8082)",
                "Servicio Matrícula (puerto 8083)",
                "Servicio Académico (puerto 8084)",
                "Servicio Reportes (puerto 8085)"
        });
        response.put("documentation_url", "/swagger-ui.html");
        return Mono.just(response);
    }

    @GetMapping("/routes")
    @Operation(summary = "Listar rutas del Gateway",
            description = "Retorna todas las rutas configuradas en el gateway")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rutas listadas exitosamente")
    })
    public Mono<Map<String, Object>> getRoutes() {
        log.info("Routes del gateway");
        Map<String, Object> response = new HashMap<>();
        Map<String, String> routes = new HashMap<>();
        routes.put("/api/usuarios/**", "http://localhost:8081 (Servicio Usuarios - Auth)");
        routes.put("/api/auth/**", "http://localhost:8081 (Servicio Usuarios - Auth)");
        routes.put("/api/asistencia/**", "http://localhost:8082 (Servicio Asistencia)");
        routes.put("/api/matricula/**", "http://localhost:8083 (Servicio Matrícula)");
        routes.put("/api/academico/**", "http://localhost:8084 (Servicio Académico)");
        routes.put("/api/reportes/**", "http://localhost:8085 (Servicio Reportes)");
        response.put("routes", routes);
        return Mono.just(response);
    }
}
