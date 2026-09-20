package com.proyecto.ColegioBackend.services;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.ColegioBackend.model.Asistencia;
import com.proyecto.ColegioBackend.repository.AsistenciaRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.proyecto.ColegioBackend.model.event.AsistenciaRegistradaEvent;
import com.proyecto.ColegioBackend.factory.AsistenciaRegistradaEventFactory;

@Service
public class AsistenciaServiceImpl implements AsistenciaService {

    private static final Logger logger = LoggerFactory.getLogger(AsistenciaServiceImpl.class);

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AsistenciaRegistradaEventFactory asistenciaRegistradaEventFactory;

    @Override
    public List<Asistencia> listarTodas() {
        try {
            return asistenciaRepository.findAll();
        } catch (Exception e) {
            logger.error("Error al listar todas las asistencias: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Asistencia guardar(Asistencia asistencia) {
        try {
            Asistencia saved = asistenciaRepository.save(asistencia);
            try {
                AsistenciaRegistradaEvent event = asistenciaRegistradaEventFactory.buildEvent(saved);
                rabbitTemplate.convertAndSend("eventos.exchange", "asistencia.registrada", event);
                logger.info("Publicado evento de asistencia registrada con ID: {}", saved.getId());
            } catch (Exception e) {
                logger.warn("ADVERTENCIA: No se pudo enviar el evento de asistencia registrada a RabbitMQ (¿servidor caído?). Detalle: {}", e.getMessage());
            }
            return saved;
        } catch (Exception e) {
            logger.error("Error al guardar asistencia para el usuario {}: {}", asistencia.getUsuarioId(), e.getMessage());
            return null;
        }
    }

    @Override
    public Asistencia buscarPorId(Long id) {
        try {
            return asistenciaRepository.findById(id).orElse(null);
        } catch (Exception e) {
            logger.error("Error al buscar asistencia con ID {}: {}", id, e.getMessage());
            return null;
        }
    }

    @Override
    public void eliminar(Long id) {
        try {
            asistenciaRepository.deleteById(id);
        } catch (Exception e) {
            logger.error("Error al eliminar asistencia con ID {}: {}", id, e.getMessage());
        }
    }

    @Override
    public List<Asistencia> listarPorUsuario(Long usuarioId) {
        try {
            return asistenciaRepository.findByUsuarioId(usuarioId);
        } catch (Exception e) {
            logger.error("Error al listar asistencias para el usuario {}: {}", usuarioId, e.getMessage());
            return Collections.emptyList();
        }
    }
}