package com.proyecto.ColegioBackend.controller;

import com.proyecto.ColegioBackend.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeriaController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/enviar")
    public ResponseEntity<?> enviarMensaje(@RequestBody Map<String, Object> mensaje) {
        // El payload esperado: { "emisorId": 1, "contenido": "Hola profesor", "tipoDestinatario": "profesor" }
        
        String tipoDestinatario = (String) mensaje.get("tipoDestinatario"); // "profesor" o "apoderado"
        String routingKey = "chat." + tipoDestinatario.toLowerCase();

        // Enriquecemos el mensaje con metadata interna
        mensaje.put("timestamp", LocalDateTime.now().toString());
        // Aquí podrías sacar el ID del usuario del token JWT para mayor seguridad
        mensaje.put("estado", "ENVIADO_INTERNAMENTE");

        // Enviamos al Exchange de RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, routingKey, mensaje);

        System.out.println(">>> [Backend] Mensaje procesado internamente hacia: " + routingKey);
        
        return ResponseEntity.ok(Map.of(
            "status", "Mensaje encolado correctamente",
            "routingKey", routingKey
        ));
    }
}
