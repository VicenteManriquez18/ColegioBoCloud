package com.proyecto.ColegioBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.ColegioBackend.model.Usuario;
import com.proyecto.ColegioBackend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.proyecto.ColegioBackend.config.RabbitMQConfig;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String correo = credentials.get("correo");
        String password = credentials.get("password");
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
        
        // Validamos si existe el usuario y si la contraseña coincide con la encriptada en la BD
        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(password, usuarioOpt.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
        
        // Si todo es correcto, obtenemos al usuario y generamos su token real
        Usuario usuario = usuarioOpt.get();
        String token = generarJwt(usuario.getId(), usuario.getCorreo(), usuario.getRol());
        
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> user) {
        try {
            System.out.println("Registrando usuario: " + user.get("correo"));
            
            String password = user.get("password");
            if (password == null || password.length() < 8) {
                return ResponseEntity.badRequest().body(Map.of("error", "La contraseña debe tener al menos 8 caracteres."));
            }
            boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
            boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
            boolean hasDigit = password.chars().anyMatch(Character::isDigit);
            boolean hasSpecial = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
            if (!hasUppercase || !hasLowercase || !hasDigit || !hasSpecial) {
                return ResponseEntity.badRequest().body(Map.of("error", 
                    "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial."));
            }

            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setCorreo(user.get("correo"));
            // Encriptamos la contraseña antes de guardarla
            nuevoUsuario.setPassword(passwordEncoder.encode(password));
            nuevoUsuario.setRol(user.getOrDefault("rol", "Alumno")); // Usamos el rol de Postman, o Alumno por defecto
            nuevoUsuario.setTelefono(user.get("telefono"));
            if (user.get("cursoId") != null && !String.valueOf(user.get("cursoId")).trim().isEmpty()) {
                nuevoUsuario.setCursoId(Long.valueOf(String.valueOf(user.get("cursoId"))));
            }
            
            Usuario guardado = usuarioRepository.save(nuevoUsuario); // ¡Guardado directo!
            System.out.println("¡ÉXITO! Usuario guardado en BD con ID: " + guardado.getId());

            // Notificar internamente al Servicio Matrícula sobre el nuevo usuario de forma segura
            try {
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, Map.of(
                    "id", guardado.getId(),
                    "correo", guardado.getCorreo(),
                    "rol", guardado.getRol()
                ));
            } catch (Exception amqpEx) {
                System.err.println("ADVERTENCIA: No se pudo enviar notificación a RabbitMQ. El usuario se guardó igualmente. Detalle: " + amqpEx.getMessage());
            }
            
            return ResponseEntity.ok(Map.of("mensaje", "Usuario registrado con éxito", "id", guardado.getId()));
        } catch (DataIntegrityViolationException e) {
            System.err.println("Intento de registro fallido: Correo duplicado.");
            return ResponseEntity.badRequest().body(Map.of("error", "Este correo ya está registrado. Por favor, usa otro o inicia sesión."));
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO AL GUARDAR EN BD: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Error de BD: " + e.getMessage()));
        }
    }

    private String getJwtSecret() {
        String envSecret = System.getenv("JWT_SECRET");
        if (envSecret != null && !envSecret.isEmpty()) {
            return envSecret;
        }
        byte[] decoded = java.util.Base64.getDecoder().decode("TWlDbGF2ZVNlY3JldGFTdXBlclNlZ3VyYVlMYXJnYVBhcmFFc3RlUHJveWVjdG9EZVNwcmluZzEyMw==");
        return new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
    }

    // Método para generar un JWT REAL y FIRMADO usando Java nativo
    private String generarJwt(Long id, String correo, String rol) {
        try {
            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            long exp = (System.currentTimeMillis() / 1000) + 86400; // Expira en 24 horas
            String payload = "{\"id\":" + id + ",\"sub\":\"" + correo + "\",\"rol\":\"" + rol + "\",\"exp\":" + exp + "}";
            
            String encodedHeader = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String encodedPayload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String data = encodedHeader + "." + encodedPayload;
            
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            
            String signature = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            return data + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el token", e);
        }
    }
}