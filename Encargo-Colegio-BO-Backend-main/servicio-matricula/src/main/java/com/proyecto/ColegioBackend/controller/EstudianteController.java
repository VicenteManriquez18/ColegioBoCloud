package com.proyecto.ColegioBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import com.proyecto.ColegioBackend.model.Estudiante;
import com.proyecto.ColegioBackend.services.EstudianteService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matricula")
public class EstudianteController {

    @Autowired
    private EstudianteService matriculaService;

    @Autowired
    private WebClient webClient;

    @Value("${servicio.usuarios.url}")
    private String usuariosUrl;

    @Value("${servicio.academico.url}")
    private String academicoUrl;

    @GetMapping("/estudiantes")
    public List<Estudiante> listarEstudiantes() {
        return matriculaService.listarTodosEstudiantes();
    }

    @PostMapping("/registrar-completo")
    public ResponseEntity<?> registrarMatricula(@RequestBody Estudiante estudiante) {
        try {
            Estudiante nuevoEstudiante = matriculaService.registrarMatriculaCompleta(estudiante);
            
            // Integración con servicio-academico
            if (nuevoEstudiante.getUsuarioId() != null && nuevoEstudiante.getCursoId() != null) {
                try {
                    String url = academicoUrl + "/api/academico/matriculas/registrar";
                    webClient.post()
                        .uri(url)
                        .bodyValue(Map.of(
                            "usuarioId", nuevoEstudiante.getUsuarioId(),
                            "cursoId", nuevoEstudiante.getCursoId()
                        ))
                        .retrieve()
                        .toBodilessEntity()
                        .block();
                } catch (Exception ex) {
                    System.err.println("Advertencia: No se pudo registrar matrícula en el servicio académico: " + ex.getMessage());
                }
            }
            
            return new ResponseEntity<>(nuevoEstudiante, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al matricular: " + e.getMessage());
        }
    }

    @GetMapping("/estudiantes/rut/{rut}")
    public ResponseEntity<Estudiante> buscarPorRut(@PathVariable String rut) {
        Estudiante estudiante = matriculaService.buscarEstudiantePorRut(rut);
        return estudiante != null ? ResponseEntity.ok(estudiante) : ResponseEntity.notFound().build();
    }

    @GetMapping("/estudiantes/{id}")
    public ResponseEntity<Estudiante> buscarPorId(@PathVariable Long id) {
        Estudiante estudiante = matriculaService.buscarPorId(id);
        return estudiante != null ? ResponseEntity.ok(estudiante) : ResponseEntity.notFound().build();
    }

    @GetMapping("/estudiantes/usuario/{usuarioId}")
    public ResponseEntity<Estudiante> buscarPorUsuarioId(@PathVariable Long usuarioId) {
        java.util.Optional<Estudiante> estudiante = estudianteRepository.findByUsuarioId(usuarioId);
        return estudiante.isPresent() ? ResponseEntity.ok(estudiante.get()) : ResponseEntity.notFound().build();
    }

    @Autowired
    private com.proyecto.ColegioBackend.repository.ApoderadoRepository apoderadoRepository;

    @Autowired
    private com.proyecto.ColegioBackend.repository.EstudianteRepository estudianteRepository;

    @GetMapping("/apoderados/me/estudiantes")
    public ResponseEntity<?> obtenerMisEstudiantes(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> claims = parseToken(token);
        if (claims == null || claims.get("id") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autorizado"));
        }
        Long guardianUserId = (Long) claims.get("id");
        java.util.Optional<com.proyecto.ColegioBackend.model.Apoderado> apoderadoOpt = apoderadoRepository.findByUsuarioId(guardianUserId);
        if (apoderadoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(List.of()); // Retorna lista vacía si no tiene ficha aún
        }
        List<Estudiante> estudiantes = estudianteRepository.findByApoderadoId(apoderadoOpt.get().getId());
        return ResponseEntity.ok(estudiantes);
    }

    // ==================== GET ALUMNOS DESDE USUARIOS ====================

    @GetMapping("/alumnos")
    public ResponseEntity<?> obtenerAlumnos(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String url = usuariosUrl + "/api/usuarios";
            WebClient.RequestHeadersSpec<?> request = webClient.get().uri(url);
            if (token != null) {
                request = request.header("Authorization", token);
            }
            ResponseEntity<List> response = request.retrieve().toEntity(List.class).block();
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al conectar con el servicio de usuarios: " + e.getMessage()));
        }
    }

    @GetMapping("/usuarios/rol/{rol}")
    public ResponseEntity<?> obtenerUsuariosPorRol(@PathVariable String rol, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String url = usuariosUrl + "/api/usuarios?rol=" + rol;
            WebClient.RequestHeadersSpec<?> request = webClient.get().uri(url);
            if (token != null) {
                request = request.header("Authorization", token);
            }
            ResponseEntity<List> response = request.retrieve().toEntity(List.class).block();
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al conectar con el servicio de usuarios: " + e.getMessage()));
        }
    }

    @PutMapping("/estudiantes/usuario/{usuarioId}")
    public ResponseEntity<?> actualizarEstudiantePorUsuarioId(@PathVariable Long usuarioId, @RequestBody Map<String, Object> body) {
        java.util.Optional<Estudiante> estudianteOpt = estudianteRepository.findByUsuarioId(usuarioId);
        if (estudianteOpt.isPresent()) {
            Estudiante estudiante = estudianteOpt.get();
            if (body.containsKey("cursoId")) {
                Object cId = body.get("cursoId");
                estudiante.setCursoId(cId != null ? Long.valueOf(cId.toString()) : null);
            }
            if (body.containsKey("nombre")) {
                estudiante.setNombre((String) body.get("nombre"));
            }
            if (body.containsKey("estado")) {
                estudiante.setEstado((String) body.get("estado"));
            }
            if (estudiante.getApoderado() != null) {
                if (body.containsKey("telefono")) {
                    estudiante.getApoderado().setTelefono((String) body.get("telefono"));
                }
                if (body.containsKey("correo")) {
                    estudiante.getApoderado().setCorreo((String) body.get("correo"));
                }
                apoderadoRepository.save(estudiante.getApoderado());
            }
            estudianteRepository.save(estudiante);
            return ResponseEntity.ok(estudiante);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/estudiantes/usuario/{usuarioId}")
    public ResponseEntity<?> eliminarEstudiantePorUsuarioId(@PathVariable Long usuarioId) {
        java.util.Optional<Estudiante> estudianteOpt = estudianteRepository.findByUsuarioId(usuarioId);
        if (estudianteOpt.isPresent()) {
            estudianteRepository.delete(estudianteOpt.get());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/apoderados/usuario/{usuarioId}")
    public ResponseEntity<?> eliminarApoderadoPorUsuarioId(@PathVariable Long usuarioId) {
        java.util.Optional<com.proyecto.ColegioBackend.model.Apoderado> apoderadoOpt = apoderadoRepository.findByUsuarioId(usuarioId);
        if (apoderadoOpt.isPresent()) {
            apoderadoRepository.delete(apoderadoOpt.get());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private Map<String, Object> parseToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String[] parts = token.split("\\.");
                if (parts.length == 3) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), "UTF-8");
                    Long id = null;
                    String rol = null;
                    
                    int idIndex = payload.indexOf("\"id\":");
                    if (idIndex != -1) {
                        int start = idIndex + 5;
                        int end = payload.indexOf(",", start);
                        if (end == -1) end = payload.indexOf("}", start);
                        id = Long.parseLong(payload.substring(start, end).trim());
                    }
                    
                    int rolIndex = payload.indexOf("\"rol\":\"");
                    if (rolIndex != -1) {
                        int start = rolIndex + 7;
                        int end = payload.indexOf("\"", start);
                        rol = payload.substring(start, end);
                    }
                    
                    return Map.of("id", id, "rol", rol);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        return null;
    }
}
