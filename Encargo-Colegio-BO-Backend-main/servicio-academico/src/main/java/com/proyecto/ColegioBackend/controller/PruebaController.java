package com.proyecto.ColegioBackend.controller;

import com.proyecto.ColegioBackend.model.Prueba;
import com.proyecto.ColegioBackend.service.AcademicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/academico/pruebas")
public class PruebaController {

    private final AcademicoService academicoService;

    private static final String ERROR_KEY = "error";

    public PruebaController(AcademicoService academicoService) {
        this.academicoService = academicoService;
    }

    @PostMapping("/curso/{cursoId}")
    public ResponseEntity<Object> crearPrueba(
            @PathVariable Long cursoId,
            @RequestBody Prueba prueba,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Map<String, Object> claims = parseToken(token);
            if (claims.isEmpty() || claims.get("rol") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(ERROR_KEY, "No autorizado"));
            }
            
            String rol = (String) claims.get("rol");
            Long profesorId = (Long) claims.get("id");
            
            if (!"Profesor".equalsIgnoreCase(rol)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(ERROR_KEY, "Solo los profesores pueden crear pruebas"));
            }
            
            if (prueba.getAsignatura() == null || prueba.getAsignatura().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "La asignatura es requerida"));
            }
            
            // Validar que el profesor esté asignado a esta asignatura en este curso
            com.proyecto.ColegioBackend.model.CursoAsignatura ca = academicoService.obtenerAsignacionPorCursoYAsignatura(cursoId, prueba.getAsignatura());
            if (ca == null || ca.getProfesorId() == null || !ca.getProfesorId().equals(profesorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(ERROR_KEY, "Usted no es el profesor designado para la asignatura " + prueba.getAsignatura() + " en este curso"));
            }
            
            Prueba nuevaPrueba = academicoService.crearPrueba(cursoId, prueba);
            return new ResponseEntity<>(nuevaPrueba, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    private Map<String, Object> parseToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Map.of();
        }
        String token = authHeader.substring(7);
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), java.nio.charset.StandardCharsets.UTF_8);
                Long id = extractLong(payload, "id");
                String rol = extractString(payload, "rol");
                if (id != null && rol != null) {
                    return Map.of("id", id, "rol", rol);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return Map.of();
    }

    private Long extractLong(String payload, String key) {
        String value = extractString(payload, key);
        return value != null ? Long.parseLong(value) : null;
    }

    private String extractString(String payload, String key) {
        String search = "\"" + key + "\":\"";
        int start = payload.indexOf(search);
        if (start != -1) {
            start += search.length();
            int end = payload.indexOf("\"", start);
            return payload.substring(start, end);
        }
        search = "\"" + key + "\":";
        start = payload.indexOf(search);
        if (start != -1) {
            start += search.length();
            int end = payload.indexOf(",", start);
            if (end == -1) end = payload.indexOf("}", start);
            return payload.substring(start, end).trim();
        }
        return null;
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<Prueba>> listarPorCurso(@PathVariable Long cursoId) {
        return ResponseEntity.ok(academicoService.listarPruebasPorCurso(cursoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prueba> obtenerPorId(@PathVariable Long id) {
        Prueba prueba = academicoService.obtenerPruebaPorId(id);
        return prueba != null ? ResponseEntity.ok(prueba) : ResponseEntity.notFound().build();
    }
}
