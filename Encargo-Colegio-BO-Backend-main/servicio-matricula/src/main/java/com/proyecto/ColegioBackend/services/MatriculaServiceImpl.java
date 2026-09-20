package com.proyecto.ColegioBackend.services;

import com.proyecto.ColegioBackend.model.Apoderado;
import com.proyecto.ColegioBackend.model.Estudiante;
import com.proyecto.ColegioBackend.repository.ApoderadoRepository;
import com.proyecto.ColegioBackend.repository.EstudianteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.proyecto.ColegioBackend.model.event.MatriculaRegistradaEvent;
import com.proyecto.ColegioBackend.factory.MatriculaRegistradaEventFactory;

@SuppressWarnings("null")
@Service
public class MatriculaServiceImpl implements EstudianteService {

    private static final Logger logger = LoggerFactory.getLogger(MatriculaServiceImpl.class);

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private ApoderadoRepository apoderadoRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MatriculaRegistradaEventFactory matriculaRegistradaEventFactory;

    @Value("${servicio.usuarios.url}")
    private String usuariosUrl;

    @Override
    public List<Estudiante> listarTodosEstudiantes() {
        try {
            return estudianteRepository.findAll();
        } catch (Exception e) {
            logger.error("Error al obtener la lista de estudiantes: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Autowired
    private org.springframework.web.reactive.function.client.WebClient webClient;

    private boolean verificarRolUsuario(Long usuarioId, String rolEsperado) {
        if (usuarioId == null) return false;
        try {
            String url = usuariosUrl + "/api/usuarios/" + usuarioId;
            java.util.Map response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(java.util.Map.class)
                .block();
            if (response != null && response.containsKey("rol")) {
                String rol = (String) response.get("rol");
                return rolEsperado.equalsIgnoreCase(rol);
            }
        } catch (Exception e) {
            logger.error("Error al verificar rol de usuario ID {}: {}", usuarioId, e.getMessage());
        }
        return false;
    }

    @Override
    @Transactional
    public Estudiante registrarMatriculaCompleta(Estudiante estudiante) {
        try {
            // Validar rol de alumno
            if (estudiante.getUsuarioId() != null) {
                if (!verificarRolUsuario(estudiante.getUsuarioId(), "Alumno")) {
                    throw new IllegalArgumentException("La cuenta vinculada al alumno debe tener el rol de 'Alumno'");
                }
            } else {
                throw new IllegalArgumentException("Se requiere vincular el alumno a una cuenta de usuario");
            }

            Apoderado apoderadoEntrada = estudiante.getApoderado();
            if (apoderadoEntrada != null) {
                // Validar rol de apoderado
                if (apoderadoEntrada.getUsuarioId() != null) {
                    if (!verificarRolUsuario(apoderadoEntrada.getUsuarioId(), "Apoderado")) {
                        throw new IllegalArgumentException("La cuenta vinculada al apoderado debe tener el rol de 'Apoderado'");
                    }
                } else {
                    throw new IllegalArgumentException("Se requiere vincular el apoderado a una cuenta de usuario");
                }

                // Buscamos si el apoderado ya existe por su RUT o por su usuarioId
                Optional<Apoderado> apoderadoExistente = apoderadoRepository.findByRut(apoderadoEntrada.getRut());
                if (apoderadoExistente.isEmpty() && apoderadoEntrada.getUsuarioId() != null) {
                    apoderadoExistente = apoderadoRepository.findByUsuarioId(apoderadoEntrada.getUsuarioId());
                }
                
                // Si existe lo asignamos, si no, guardamos el nuevo
                Apoderado apoderado = apoderadoExistente.orElseGet(() -> apoderadoRepository.save(apoderadoEntrada));
                // Asegurar que guarde los datos correctos si ya existía pero con campos actualizados
                if (apoderadoExistente.isPresent()) {
                    Apoderado e = apoderadoExistente.get();
                    e.setNombre(apoderadoEntrada.getNombre());
                    e.setTelefono(apoderadoEntrada.getTelefono());
                    e.setCorreo(apoderadoEntrada.getCorreo());
                    e.setUsuarioId(apoderadoEntrada.getUsuarioId());
                    apoderado = apoderadoRepository.save(e);
                }
                estudiante.setApoderado(apoderado);
            }
            Estudiante saved = estudianteRepository.save(estudiante);
            try {
                MatriculaRegistradaEvent event = matriculaRegistradaEventFactory.buildEvent(saved);
                rabbitTemplate.convertAndSend("eventos.exchange", "matricula.registrada", event);
                logger.info("Publicado evento de matrícula registrada con ID: {}", saved.getId());
            } catch (Exception e) {
                logger.warn("ADVERTENCIA: No se pudo enviar el evento de matrícula registrada a RabbitMQ (¿servidor caído?). Detalle: {}", e.getMessage());
            }
            return saved;
        } catch (Exception e) {
            logger.error("Error crítico al registrar matrícula para {}: {}", estudiante.getNombre(), e.getMessage());
            throw e; // Lanzamos la excepción para que @Transactional haga el rollback si es necesario
        }
    }

    @Override
    public Estudiante buscarPorId(Long id) {
        try {
            return estudianteRepository.findById(id).orElse(null);
        } catch (Exception e) {
            logger.error("Error al buscar estudiante con ID {}: {}", id, e.getMessage());
            return null;
        }
    }

    @Override
    public Estudiante buscarEstudiantePorRut(String rut) {
        try {
            // Se asume que el repositorio tiene el método findByRut
            return estudianteRepository.findByRut(rut).orElse(null);
        } catch (Exception e) {
            logger.error("Error al buscar estudiante con RUT {}: {}", rut, e.getMessage());
            return null;
        }
    }
}
