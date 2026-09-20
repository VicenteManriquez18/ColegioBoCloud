package com.proyecto.mensajeria.service;

import com.proyecto.mensajeria.config.RabbitMQConfig;
import com.proyecto.mensajeria.entity.Mensaje;
import com.proyecto.mensajeria.exception.UsuarioServicioNoDisponibleException;
import com.proyecto.mensajeria.repository.MensajeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class MensajeServiceImpl implements MensajeService {

    private static final Logger log = LoggerFactory.getLogger(MensajeServiceImpl.class);

    private static final String ROLE_PROFESOR = "Profesor";
    private static final String ROLE_APODERADO = "Apoderado";

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private WebClient webClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${servicio.usuarios.url}")
    private String usuariosServiceUrl;

    @SuppressWarnings("unchecked")
    private Map<String, Object> obtenerUsuarioConCircuitBreaker(Long id) {
        String url = usuariosServiceUrl + "/api/usuarios/" + id;
        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception throwable) {
            log.error("Falló la conexión con el servicio de usuarios para el ID {}. Detalle: {}", id, throwable.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> obtenerUsuariosPorRolConCircuitBreaker(String rol) {
        String url = usuariosServiceUrl + "/api/usuarios?rol=" + rol;
        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
        } catch (Exception throwable) {
            log.error("Falló la conexión con el servicio de usuarios para listar rol {}. Detalle: {}", rol, throwable.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Mensaje enviarMensaje(Long remitenteId, Long destinatarioId, String contenido) {
        // 1. Obtener emisor y receptor usando Circuit Breaker
        Map<String, Object> emisor = obtenerUsuarioConCircuitBreaker(remitenteId);
        Map<String, Object> receptor = obtenerUsuarioConCircuitBreaker(destinatarioId);

        if (emisor == null || receptor == null) {
            throw new UsuarioServicioNoDisponibleException("No se pudo verificar la información de los usuarios debido a que el servicio de usuarios no responde.");
        }

        String emisorRol = (String) emisor.get("rol");
        String receptorRol = (String) receptor.get("rol");
        String emisorCorreo = (String) emisor.get("correo");
        String receptorCorreo = (String) receptor.get("correo");

        // 2. Validar que la comunicación sea Profesor <-> Apoderado exclusivamente
        boolean esEmisorProfesor = ROLE_PROFESOR.equalsIgnoreCase(emisorRol);
        boolean esEmisorApoderado = ROLE_APODERADO.equalsIgnoreCase(emisorRol);
        boolean esReceptorProfesor = ROLE_PROFESOR.equalsIgnoreCase(receptorRol);
        boolean esReceptorApoderado = ROLE_APODERADO.equalsIgnoreCase(receptorRol);

        boolean esValido = (esEmisorProfesor && esReceptorApoderado) || (esEmisorApoderado && esReceptorProfesor);

        if (!esValido) {
            throw new IllegalArgumentException("La mensajería está restringida exclusivamente para la comunicación entre un Profesor y un Apoderado.");
        }

        // 3. Crear el mensaje
        Mensaje mensaje = Mensaje.builder()
                .remitenteId(remitenteId)
                .remitenteNombre(emisorCorreo)
                .remitenteRol(emisorRol)
                .destinatarioId(destinatarioId)
                .destinatarioNombre(receptorCorreo)
                .destinatarioRol(receptorRol)
                .contenido(contenido)
                .fechaEnvio(LocalDateTime.now())
                .build();

        // 4. Encolar en RabbitMQ
        String routingKey = "chat." + receptorRol.toLowerCase();
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, routingKey, mensaje);
            log.info("Mensaje encolado en RabbitMQ con routingKey: {} de {} para {}", routingKey, emisorCorreo, receptorCorreo);
        } catch (Exception amqpEx) {
            log.warn("ADVERTENCIA: No se pudo enviar el mensaje a RabbitMQ (¿servidor caído?). Guardando directamente en base de datos como fallback. Detalle: {}", amqpEx.getMessage());
            mensaje = mensajeRepository.save(mensaje);
        }

        return mensaje;
    }

    @Override
    public List<Mensaje> obtenerHistorial(Long user1, Long user2) {
        return mensajeRepository.findChatHistory(user1, user2);
    }

    @Override
    public List<Map<String, Object>> obtenerContactos(Long userId) {
        Map<String, Object> usuario = obtenerUsuarioConCircuitBreaker(userId);
        if (usuario == null) {
            throw new UsuarioServicioNoDisponibleException("No se pudo verificar la información del usuario debido a que el servicio de usuarios no responde.");
        }

        String rol = (String) usuario.get("rol");
        if (ROLE_PROFESOR.equalsIgnoreCase(rol)) {
            // Retorna todos los apoderados
            return obtenerUsuariosPorRolConCircuitBreaker(ROLE_APODERADO);
        } else if (ROLE_APODERADO.equalsIgnoreCase(rol)) {
            // Retorna todos los profesores
            return obtenerUsuariosPorRolConCircuitBreaker(ROLE_PROFESOR);
        }

        return Collections.emptyList();
    }
}
