package com.proyecto.ColegioBackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.ColegioBackend.model.Usuario;
import com.proyecto.ColegioBackend.services.UsuarioService;

import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/usuarios")

public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService; // Inyectamos la interfaz, no la implementación

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<Usuario> listar(@org.springframework.web.bind.annotation.RequestParam(required = false) String rol) {
        if (rol != null && !rol.isEmpty()) {
            return usuarioService.listarPorRol(rol);
        }
        return usuarioService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Usuario usuario) {
        String password = usuario.getPassword();
        if (password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "La contraseña debe tener al menos 8 caracteres."));
        }
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        if (!hasUppercase || !hasLowercase || !hasDigit || !hasSpecial) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", 
                "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial."));
        }

        usuario.setPassword(passwordEncoder.encode(password));
        return new ResponseEntity<>(usuarioService.guardar(usuario), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        Usuario existente = usuarioService.buscarPorId(id);
        if (existente != null) {
            usuario.setId(id); // Aseguramos que el ID sea el correcto
            // Si se proporciona nueva contraseña, la encriptamos
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            } else {
                // Si no se proporciona, mantenemos la existente
                usuario.setPassword(existente.getPassword());
            }
            
            Usuario guardado = usuarioService.guardar(usuario);
            
            // Sync with other microservices
            try {
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                
                if ("Alumno".equalsIgnoreCase(guardado.getRol())) {
                    // Sync student info
                    java.util.Map<String, Object> payload = new java.util.HashMap<>();
                    payload.put("telefono", guardado.getTelefono());
                    payload.put("correo", guardado.getCorreo());
                    if (guardado.getCursoId() != null) {
                        payload.put("cursoId", guardado.getCursoId());
                    }
                    
                    String jsonPayload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
                    
                    java.net.http.HttpRequest reqMatricula = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://servicio-matricula:8083/api/matricula/estudiantes/usuario/" + id))
                        .header("Content-Type", "application/json")
                        .PUT(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();
                    client.send(reqMatricula, java.net.http.HttpResponse.BodyHandlers.discarding());
                    
                    // Sync course enrollment in academic service
                    if (guardado.getCursoId() != null) {
                        java.util.Map<String, Object> acadPayload = java.util.Map.of(
                            "usuarioId", id,
                            "cursoId", guardado.getCursoId()
                        );
                        String acadJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(acadPayload);
                        java.net.http.HttpRequest reqAcademico = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create("http://servicio-academico:8084/api/academico/matriculas/registrar"))
                            .header("Content-Type", "application/json")
                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(acadJson))
                            .build();
                        client.send(reqAcademico, java.net.http.HttpResponse.BodyHandlers.discarding());
                    }
                } else if ("Profesor".equalsIgnoreCase(guardado.getRol())) {
                    if (guardado.getCursoId() != null) {
                        java.util.Map<String, Object> acadPayload = java.util.Map.of(
                            "profesorId", id
                        );
                        String acadJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(acadPayload);
                        java.net.http.HttpRequest reqAcademico = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create("http://servicio-academico:8084/api/academico/cursos/" + guardado.getCursoId() + "/profesor"))
                            .header("Content-Type", "application/json")
                            .PUT(java.net.http.HttpRequest.BodyPublishers.ofString(acadJson))
                            .build();
                        client.send(reqAcademico, java.net.http.HttpResponse.BodyHandlers.discarding());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error syncing update with microservices: " + e.getMessage());
            }
            
            return ResponseEntity.ok(guardado);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Usuario existente = usuarioService.buscarPorId(id);
        if (existente != null) {
            // Sync deletion with other microservices
            try {
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                
                if ("Alumno".equalsIgnoreCase(existente.getRol())) {
                    // Delete in servicio-matricula
                    java.net.http.HttpRequest reqMatricula = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://servicio-matricula:8083/api/matricula/estudiantes/usuario/" + id))
                        .DELETE()
                        .build();
                    client.send(reqMatricula, java.net.http.HttpResponse.BodyHandlers.discarding());
                    
                    // Delete in servicio-academico
                    java.net.http.HttpRequest reqAcademico = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://servicio-academico:8084/api/academico/matriculas/alumno/" + id))
                        .DELETE()
                        .build();
                    client.send(reqAcademico, java.net.http.HttpResponse.BodyHandlers.discarding());
                } else if ("Apoderado".equalsIgnoreCase(existente.getRol())) {
                    // Delete in servicio-matricula
                    java.net.http.HttpRequest reqMatricula = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://servicio-matricula:8083/api/matricula/apoderados/usuario/" + id))
                        .DELETE()
                        .build();
                    client.send(reqMatricula, java.net.http.HttpResponse.BodyHandlers.discarding());
                }
            } catch (Exception e) {
                System.err.println("Error syncing deletion with microservices: " + e.getMessage());
            }

            usuarioService.eliminar(id);
        }
        return ResponseEntity.noContent().build();
    }

}
