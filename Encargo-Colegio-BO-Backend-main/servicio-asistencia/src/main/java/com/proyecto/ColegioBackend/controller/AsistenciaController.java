package com.proyecto.ColegioBackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import com.proyecto.ColegioBackend.model.Asistencia;
import com.proyecto.ColegioBackend.repository.AsistenciaRepository;
import com.proyecto.ColegioBackend.services.AsistenciaService;
import java.util.Map;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/api/asistencia")
public class AsistenciaController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AsistenciaController.class);

    private final AsistenciaRepository asistenciaRepository;
    private final AsistenciaService asistenciaService;
    private final WebClient webClient;

    private static final String ERROR_KEY = "error";
    private static final String TIMEZONE = "America/Santiago";

    @Value("${servicio.usuarios.url}")
    private String usuariosUrl;

    @Value("${servicio.matricula.url}")
    private String matriculaUrl;

    @Value("${servicio.academico.url}")
    private String academicoUrl;

    public AsistenciaController(AsistenciaRepository asistenciaRepository, AsistenciaService asistenciaService, WebClient webClient) {
        this.asistenciaRepository = asistenciaRepository;
        this.asistenciaService = asistenciaService;
        this.webClient = webClient;
    }

    // ==================== GET ====================
    
    @GetMapping
    public List<Asistencia> listar() {
        return asistenciaService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asistencia> buscarPorId(@PathVariable Long id) {
        Asistencia asistencia = asistenciaService.buscarPorId(id);
        if (asistencia != null) {
            return ResponseEntity.ok(asistencia);
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== POST ====================
    
    @PostMapping
    public ResponseEntity<Object> guardar(
            @RequestBody Asistencia asistencia,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        Map<String, Object> claims = parseToken(token);
        if (claims.isEmpty() || claims.get("rol") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(ERROR_KEY, "No autorizado"));
        }
        
        String rol = (String) claims.get("rol");
        Long profesorId = (Long) claims.get("id");
        
        if (!"Profesor".equalsIgnoreCase(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(ERROR_KEY, "Solo los profesores pueden registrar asistencia"));
        }
        
        if (asistencia.getCursoId() == null || asistencia.getAsignatura() == null) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "Los campos 'cursoId' y 'asignatura' son requeridos"));
        }
        
        // 1. Validar que el profesor esté asignado a la asignatura en este curso
        if (!verificarAsignacionProfesor(asistencia.getCursoId(), asistencia.getAsignatura(), profesorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(ERROR_KEY, "Usted no es el profesor designado para la asignatura '" + asistencia.getAsignatura() + "' en este curso"));
        }
        
        // 2. Validar que el estudiante esté matriculado en el curso
        if (!verificarMatriculaCurso(asistencia.getUsuarioId(), asistencia.getCursoId())) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "El estudiante con ID de usuario " + asistencia.getUsuarioId() + " no está matriculado en este curso"));
        }
        
        asistencia.setFecha(LocalDateTime.now(ZoneId.of(TIMEZONE)));
        Asistencia guardada = asistenciaService.guardar(asistencia);
        return new ResponseEntity<>(guardada, HttpStatus.CREATED);
    }

    private Map<String, Object> parseToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Map.of();
        }
        String token = authHeader.substring(7);
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
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

    private boolean verificarMatriculaCurso(Long studentUserId, Long cursoId) {
        try {
            String url = matriculaUrl + "/api/matricula/estudiantes/usuario/" + studentUserId;
            Map<String, Object> response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
            if (response != null && response.containsKey("cursoId")) {
                Object cId = response.get("cursoId");
                if (cId != null) {
                    Long actualCursoId = Long.valueOf(cId.toString());
                    return cursoId.equals(actualCursoId);
                }
            }
        } catch (Exception e) {
            log.error("Error al verificar matrícula para usuarioId {}: {}", studentUserId, e.getMessage());
        }
        return false;
    }

    private boolean verificarAsignacionProfesor(Long cursoId, String asignatura, Long profesorId) {
        try {
            String url = academicoUrl + "/api/academico/cursos/" + cursoId + "/asignaturas";
            List<Map<String, Object>> response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
            if (response != null) {
                for (Object item : response) {
                    if (item instanceof Map<?, ?> map) {
                        String asigName = (String) map.get("asignatura");
                        if (asignatura.equalsIgnoreCase(asigName)) {
                            Object profIdObj = map.get("profesorId");
                            if (profIdObj != null) {
                                Long actualProfesorId = Long.valueOf(profIdObj.toString());
                                return profesorId.equals(actualProfesorId);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error al verificar asignación de profesor para cursoId {}: {}", cursoId, e.getMessage());
        }
        return false;
    }

    @PostMapping("/registrar")
    public ResponseEntity<Object> registrarAsistencia(@RequestBody Map<String, Object> request) {
        Object idObj = request.get("usuarioId");
        if (idObj == null) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "El campo usuarioId es requerido"));
        }

        Long usuarioId = Long.valueOf(idObj.toString());

        try {
            String url = usuariosUrl + "/api/usuarios/" + usuarioId;
            ResponseEntity<Map<String, Object>> response = webClient.get()
                .uri(url)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
            Map<String, Object> usuario = response != null ? response.getBody() : null;

            if (usuario == null || !usuario.containsKey("correo")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(ERROR_KEY, "Usuario no encontrado en el sistema central"));
            }

            Asistencia nuevaAsistencia = new Asistencia();
            nuevaAsistencia.setUsuarioId(usuarioId);
            nuevaAsistencia.setNombreUsuario((String) usuario.get("correo"));
            nuevaAsistencia.setFecha(LocalDateTime.now(ZoneId.of(TIMEZONE)));

            return ResponseEntity.ok(asistenciaService.guardar(nuevaAsistencia));
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(ERROR_KEY, "ID de usuario inexistente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(ERROR_KEY, "Error al conectar con el servicio de usuarios: " + e.getMessage()));
        }
    }

    // ==================== GET ALUMNOS DESDE USUARIOS ====================

    @GetMapping("/alumnos")
    public ResponseEntity<List<Map<String, Object>>> obtenerAlumnos(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String url = usuariosUrl + "/api/usuarios";
            WebClient.RequestHeadersSpec<?> request = webClient.get().uri(url);
            if (token != null) {
                request = request.header("Authorization", token);
            }
            ResponseEntity<List<Map<String, Object>>> response = request.retrieve()
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
            return ResponseEntity.ok(response != null ? response.getBody() : List.of());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }

    // ==================== GET BY ALUMNO ====================

    @GetMapping("/alumno/{usuarioId}")
    public ResponseEntity<List<Asistencia>> buscarPorUsuarioId(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(asistenciaService.listarPorUsuario(usuarioId));
    }

    // ==================== DELETE ====================
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        asistenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}