package com.proyecto.mensajeria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import com.proyecto.mensajeria.entity.Mensaje;
import com.proyecto.mensajeria.repository.MensajeRepository;
import com.proyecto.mensajeria.exception.UsuarioServicioNoDisponibleException;

@ExtendWith(MockitoExtension.class)
public class MensajeServiceImplTest {

    @Mock
    private MensajeRepository mensajeRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private MensajeServiceImpl mensajeService;

    private final String usuariosServiceUrl = "http://localhost:8081";

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mensajeService, "usuariosServiceUrl", usuariosServiceUrl);

        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @SuppressWarnings("unchecked")
    @Test
    void enviarMensaje_ProfesorParaApoderado_Exito() {
        Map<String, Object> emisor = Map.of("id", 1L, "rol", "Profesor", "correo", "profe@colegio.com");
        Map<String, Object> receptor = Map.of("id", 2L, "rol", "Apoderado", "correo", "apoderado@colegio.com");

        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(emisor))
                .thenReturn(Mono.just(receptor));

        Mensaje result = mensajeService.enviarMensaje(1L, 2L, "Hola, ¿cómo está?");

        assertNotNull(result);
        assertEquals(1L, result.getRemitenteId());
        assertEquals(2L, result.getDestinatarioId());
        assertEquals("Hola, ¿cómo está?", result.getContenido());
        assertEquals("Profesor", result.getRemitenteRol());
        assertEquals("Apoderado", result.getDestinatarioRol());

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Mensaje.class));
        verify(mensajeRepository, never()).save(any(Mensaje.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void enviarMensaje_ApoderadoParaProfesor_Exito() {
        Map<String, Object> emisor = Map.of("id", 2L, "rol", "Apoderado", "correo", "apoderado@colegio.com");
        Map<String, Object> receptor = Map.of("id", 1L, "rol", "Profesor", "correo", "profe@colegio.com");

        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(emisor))
                .thenReturn(Mono.just(receptor));

        Mensaje result = mensajeService.enviarMensaje(2L, 1L, "Hola, todo bien.");

        assertNotNull(result);
        assertEquals(2L, result.getRemitenteId());
        assertEquals(1L, result.getDestinatarioId());
        assertEquals("Hola, todo bien.", result.getContenido());

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Mensaje.class));
        verify(mensajeRepository, never()).save(any(Mensaje.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void enviarMensaje_ProfesorParaProfesor_DebeLanzarIllegalArgumentException() {
        Map<String, Object> emisor = Map.of("id", 1L, "rol", "Profesor", "correo", "profe1@colegio.com");
        Map<String, Object> receptor = Map.of("id", 3L, "rol", "Profesor", "correo", "profe2@colegio.com");

        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(emisor))
                .thenReturn(Mono.just(receptor));

        assertThrows(IllegalArgumentException.class, () -> {
            mensajeService.enviarMensaje(1L, 3L, "Hola colega");
        });

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Mensaje.class));
        verify(mensajeRepository, never()).save(any(Mensaje.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void enviarMensaje_ServicioUsuariosCaido_DebeLanzarUsuarioServicioNoDisponibleException() {
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.error(new RuntimeException("Error de conexión")));

        assertThrows(UsuarioServicioNoDisponibleException.class, () -> {
            mensajeService.enviarMensaje(1L, 2L, "Hola");
        });

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Mensaje.class));
        verify(mensajeRepository, never()).save(any(Mensaje.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void enviarMensaje_RabbitMQCaido_GuardarEnBDDirectamente() {
        Map<String, Object> emisor = Map.of("id", 1L, "rol", "Profesor", "correo", "profe@colegio.com");
        Map<String, Object> receptor = Map.of("id", 2L, "rol", "Apoderado", "correo", "apoderado@colegio.com");

        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(emisor))
                .thenReturn(Mono.just(receptor));

        doThrow(new RuntimeException("RabbitMQ Connection Refused"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Mensaje.class));

        Mensaje mockSaved = Mensaje.builder()
                .id(100L)
                .remitenteId(1L)
                .destinatarioId(2L)
                .contenido("Hola, RabbitMQ está caído")
                .build();
        when(mensajeRepository.save(any(Mensaje.class))).thenReturn(mockSaved);

        Mensaje result = mensajeService.enviarMensaje(1L, 2L, "Hola, RabbitMQ está caído");

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(mensajeRepository, times(1)).save(any(Mensaje.class));
    }

    @Test
    void obtenerHistorial_Exito() {
        List<Mensaje> mockHistorial = Arrays.asList(
                Mensaje.builder().id(1L).remitenteId(1L).destinatarioId(2L).contenido("Hola").build(),
                Mensaje.builder().id(2L).remitenteId(2L).destinatarioId(1L).contenido("Chao").build());

        when(mensajeRepository.findChatHistory(1L, 2L)).thenReturn(mockHistorial);

        List<Mensaje> result = mensajeService.obtenerHistorial(1L, 2L);

        assertEquals(2, result.size());
        assertEquals("Hola", result.get(0).getContenido());
        assertEquals("Chao", result.get(1).getContenido());
        verify(mensajeRepository, times(1)).findChatHistory(1L, 2L);
    }

    @SuppressWarnings("unchecked")
    @Test
    void obtenerContactos_Profesor_RetornaApoderados() {
        Map<String, Object> usuario = Map.of("id", 1L, "rol", "Profesor");
        List<Map<String, Object>> apoderados = List.of(
                Map.of("id", 2L, "rol", "Apoderado", "correo", "apo1@colegio.com"),
                Map.of("id", 3L, "rol", "Apoderado", "correo", "apo2@colegio.com"));

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(usuario));
        when(responseSpec.bodyToMono(List.class)).thenReturn(Mono.just(apoderados));

        List<Map<String, Object>> result = mensajeService.obtenerContactos(1L);

        assertEquals(2, result.size());
        assertEquals("apo1@colegio.com", result.get(0).get("correo"));
        verify(requestHeadersSpec, times(2)).retrieve(); // 1 for get user, 1 for get by role
    }

    @SuppressWarnings("unchecked")
    @Test
    void obtenerContactos_Apoderado_RetornaProfesores() {
        Map<String, Object> usuario = Map.of("id", 2L, "rol", "Apoderado");
        List<Map<String, Object>> profesores = List.of(
                Map.of("id", 1L, "rol", "Profesor", "correo", "profe@colegio.com"));

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(usuario));
        when(responseSpec.bodyToMono(List.class)).thenReturn(Mono.just(profesores));

        List<Map<String, Object>> result = mensajeService.obtenerContactos(2L);

        assertEquals(1, result.size());
        assertEquals("profe@colegio.com", result.get(0).get("correo"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void obtenerContactos_UsuarioConRolDesconocido_RetornaVacio() {
        Map<String, Object> usuario = Map.of("id", 4L, "rol", "Administrador");

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(usuario));

        List<Map<String, Object>> result = mensajeService.obtenerContactos(4L);

        assertTrue(result.isEmpty());
        verify(requestHeadersSpec, times(1)).retrieve(); // Only fetched the user, didn't call role listing
    }

    @SuppressWarnings("unchecked")
    @Test
    void obtenerContactos_ServicioUsuariosCaido_DebeLanzarUsuarioServicioNoDisponibleException() {
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(new RuntimeException("Error")));

        assertThrows(UsuarioServicioNoDisponibleException.class, () -> {
            mensajeService.obtenerContactos(1L);
        });
    }
}
