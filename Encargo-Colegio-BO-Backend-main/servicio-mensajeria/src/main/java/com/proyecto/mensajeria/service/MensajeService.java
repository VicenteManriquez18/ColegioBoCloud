package com.proyecto.mensajeria.service;

import com.proyecto.mensajeria.entity.Mensaje;
import java.util.List;
import java.util.Map;

public interface MensajeService {
    Mensaje enviarMensaje(Long remitenteId, Long destinatarioId, String contenido);
    List<Mensaje> obtenerHistorial(Long user1, Long user2);
    List<Map<String, Object>> obtenerContactos(Long userId);
}
