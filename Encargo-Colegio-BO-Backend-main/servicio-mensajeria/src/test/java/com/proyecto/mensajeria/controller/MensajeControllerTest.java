package com.proyecto.mensajeria.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.proyecto.mensajeria.entity.Mensaje;
import com.proyecto.mensajeria.service.MensajeService;
import com.proyecto.mensajeria.exception.UsuarioServicioNoDisponibleException;

@ExtendWith(MockitoExtension.class)
public class MensajeControllerTest {

    @Mock
    private MensajeService mensajeService;

    @InjectMocks
    private MensajeController mensajeController;

    @Test
    void enviarMensaje_ProfesorParaApoderado_Exito() {
        Map<String, Object> body = new HashMap<>();
        body.put("remitenteId", 1L);
        body.put("destinatarioId", 2L);
        body.put("contenido", "Hola, ¿cómo está?");

        Mensaje mockMensaje = Mensaje.builder()
                .id(1L)
                .remitenteId(1L)
                .destinatarioId(2L)
                .contenido("Hola, ¿cómo está?")
                .build();
        // septiembre 13 2026
        when(mensajeService.enviarMensaje(1L, 2L, "Hola, ¿cómo está?")).thenReturn(mockMensaje);

        ResponseEntity<?> response = mensajeController.enviarMensaje(body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mensajeService, times(1)).enviarMensaje(1L, 2L, "Hola, ¿cómo está?");
    }

    @Test
    void enviarMensaje_ApoderadoParaProfesor_Exito() {
        Map<String, Object> body = new HashMap<>();
        body.put("remitenteId", 2L);
        body.put("destinatarioId", 1L);
        body.put("contenido", "Hola, todo bien.");
        // septiembre 13 2026
        Mensaje mockMensaje = Mensaje.builder()
                .id(2L)
                .remitenteId(2L)
                .destinatarioId(1L)
                .contenido("Hola, todo bien.")
                .build();

        when(mensajeService.enviarMensaje(2L, 1L, "Hola, todo bien.")).thenReturn(mockMensaje);

        ResponseEntity<?> response = mensajeController.enviarMensaje(body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mensajeService, times(1)).enviarMensaje(2L, 1L, "Hola, todo bien.");
    }

    @Test
    void enviarMensaje_ProfesorParaProfesor_DebeRetornarBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("remitenteId", 1L);
        body.put("destinatarioId", 3L);
        body.put("contenido", "Hola colega");

        when(mensajeService.enviarMensaje(1L, 3L, "Hola colega"))
                .thenThrow(new IllegalArgumentException(
                        "La mensajería está restringida exclusivamente para la comunicación entre un Profesor y un Apoderado."));

        ResponseEntity<?> response = mensajeController.enviarMensaje(body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("La mensajería está restringida exclusivamente"));
    }

    @Test
    void enviarMensaje_ServicioUsuariosCaido_DebeRetornarServiceUnavailable() {
        Map<String, Object> body = new HashMap<>();
        body.put("remitenteId", 1L);
        body.put("destinatarioId", 2L);
        body.put("contenido", "Hola");

        when(mensajeService.enviarMensaje(1L, 2L, "Hola"))
                .thenThrow(new UsuarioServicioNoDisponibleException(
                        "No se pudo verificar la información de los usuarios debido a que el servicio de usuarios no responde."));

        ResponseEntity<?> response = mensajeController.enviarMensaje(body);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("No se pudo verificar la información"));
    }

    @Test
    void enviarMensaje_CamposRequeridosFaltantes_DebeRetornarBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("remitenteId", 1L);
        // destinatarioId y contenido faltantes

        ResponseEntity<?> response = mensajeController.enviarMensaje(body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString()
                .contains("Los campos remitenteId, destinatarioId y contenido son requeridos"));
    }

    @Test
    void obtenerHistorial_Exito() {
        List<Mensaje> mockHistorial = List.of(
                Mensaje.builder().id(1L).contenido("Hola").build());
        when(mensajeService.obtenerHistorial(1L, 2L)).thenReturn(mockHistorial);

        ResponseEntity<List<Mensaje>> response = mensajeController.obtenerHistorial(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Hola", response.getBody().get(0).getContenido());
    }

    @Test
    void obtenerContactos_Exito() {
        List<Map<String, Object>> mockContactos = List.of(
                Map.of("id", 2L, "rol", "Apoderado", "correo", "apoderado@colegio.com"));
        when(mensajeService.obtenerContactos(1L)).thenReturn(mockContactos);

        ResponseEntity<List<Map<String, Object>>> response = mensajeController.obtenerContactos(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("apoderado@colegio.com", response.getBody().get(0).get("correo"));
    }

    @Test
    void obtenerContactos_ServicioUsuariosCaido_DebeRetornarVacioConServiceUnavailable() {
        when(mensajeService.obtenerContactos(1L))
                .thenThrow(new UsuarioServicioNoDisponibleException("Servicio caído"));

        ResponseEntity<List<Map<String, Object>>> response = mensajeController.obtenerContactos(1L);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}
