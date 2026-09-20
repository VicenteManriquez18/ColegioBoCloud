package com.proyecto.mensajeria.controller;

import com.proyecto.mensajeria.entity.Mensaje;
import com.proyecto.mensajeria.service.MensajeService;
import com.proyecto.mensajeria.exception.UsuarioServicioNoDisponibleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mensajes")
@Tag(name = "Mensajería", description = "Endpoints para la comunicación entre profesores y apoderados")
public class MensajeController {

    private static final Logger log = LoggerFactory.getLogger(MensajeController.class);

    private static final String ERROR_KEY = "error";

    @Autowired
    private MensajeService mensajeService;

    @PostMapping("/enviar")
    @Operation(summary = "Enviar mensaje a través de RabbitMQ", description = "Valida que la comunicación sea exclusiva entre Profesor y Apoderado y encola el mensaje.")
    public ResponseEntity<Object> enviarMensaje(@RequestBody Map<String, Object> body) {
        try {
            Object remitenteIdObj = body.get("remitenteId");
            Object destinatarioIdObj = body.get("destinatarioId");
            String contenido = (String) body.get("contenido");

            if (remitenteIdObj == null || destinatarioIdObj == null || contenido == null || contenido.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "Los campos remitenteId, destinatarioId y contenido son requeridos."));
            }

            Long remitenteId = Long.valueOf(remitenteIdObj.toString());
            Long destinatarioId = Long.valueOf(destinatarioIdObj.toString());

            Mensaje mensaje = mensajeService.enviarMensaje(remitenteId, destinatarioId, contenido);

            return ResponseEntity.ok(Map.of(
                    "status", "Mensaje enviado correctamente",
                    "data", mensaje
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, e.getMessage()));
        } catch (UsuarioServicioNoDisponibleException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
        } catch (Exception e) {
            log.error("Error al enviar mensaje: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR_KEY, "Error interno al enviar mensaje: " + e.getMessage()));
        }
    }

    @GetMapping("/historial")
    @Operation(summary = "Obtener el historial de chat", description = "Retorna la lista ordenada de mensajes entre dos usuarios.")
    public ResponseEntity<List<Mensaje>> obtenerHistorial(@RequestParam Long user1, @RequestParam Long user2) {
        List<Mensaje> historial = mensajeService.obtenerHistorial(user1, user2);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/contactos/{userId}")
    @Operation(summary = "Obtener contactos permitidos", description = "Retorna apoderados si el usuario es profesor, o profesores si el usuario es apoderado.")
    public ResponseEntity<List<Map<String, Object>>> obtenerContactos(@PathVariable Long userId) {
        try {
            List<Map<String, Object>> contactos = mensajeService.obtenerContactos(userId);
            return ResponseEntity.ok(contactos);
        } catch (UsuarioServicioNoDisponibleException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Collections.emptyList());
        }
    }
}
