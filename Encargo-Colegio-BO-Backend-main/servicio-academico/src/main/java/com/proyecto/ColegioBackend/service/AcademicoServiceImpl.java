package com.proyecto.ColegioBackend.service;

import com.proyecto.ColegioBackend.model.Curso;
import com.proyecto.ColegioBackend.model.Matricula;
import com.proyecto.ColegioBackend.model.Prueba;
import com.proyecto.ColegioBackend.model.Nota;
import com.proyecto.ColegioBackend.repository.CursoRepository;
import com.proyecto.ColegioBackend.repository.MatriculaRepository;
import com.proyecto.ColegioBackend.repository.PruebaRepository;
import com.proyecto.ColegioBackend.repository.NotaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.proyecto.ColegioBackend.model.event.NotaGeneradaEvent;
import com.proyecto.ColegioBackend.factory.NotaGeneradaEventFactory;

@Service
public class AcademicoServiceImpl implements AcademicoService {

    private static final Logger logger = LoggerFactory.getLogger(AcademicoServiceImpl.class);
    
    private static final String CURSO_NOT_FOUND_MSG = "Curso no encontrado con ID: ";

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private PruebaRepository pruebaRepository;

    @Autowired
    private NotaRepository notaRepository;

    @Autowired
    private com.proyecto.ColegioBackend.repository.CursoAsignaturaRepository cursoAsignaturaRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NotaGeneradaEventFactory notaGeneradaEventFactory;

    // ==================== CURSOS ====================

    @Override
    public List<Curso> listarCursos() {
        return cursoRepository.findAll();
    }

    @Override
    public Curso obtenerCursoPorId(Long id) {
        return cursoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Curso crearCurso(Curso curso) {
        if (curso.getCodigo() == null || curso.getCodigo().isEmpty()) {
            // Reemplaza caracteres extraños por guiones
            String safeName = curso.getNombre().toUpperCase().replaceAll("[^A-Z0-9]", "-");
            curso.setCodigo("CUR-" + safeName);
        }
        Curso guardado = cursoRepository.save(curso);

        // Auto-seed the 4 subjects with suffix using the course ID
        String suffix = " " + guardado.getId();
        
        cursoAsignaturaRepository.save(com.proyecto.ColegioBackend.model.CursoAsignatura.builder()
            .curso(guardado)
            .asignatura("ingles" + suffix)
            .build());
        cursoAsignaturaRepository.save(com.proyecto.ColegioBackend.model.CursoAsignatura.builder()
            .curso(guardado)
            .asignatura("lenguaje" + suffix)
            .build());
        cursoAsignaturaRepository.save(com.proyecto.ColegioBackend.model.CursoAsignatura.builder()
            .curso(guardado)
            .asignatura("matematica" + suffix)
            .build());
        cursoAsignaturaRepository.save(com.proyecto.ColegioBackend.model.CursoAsignatura.builder()
            .curso(guardado)
            .asignatura("historia" + suffix)
            .build());

        return guardado;
    }

    @Override
    @Transactional
    public Curso asignarProfesor(Long cursoId, Long profesorId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException(CURSO_NOT_FOUND_MSG + cursoId));
        curso.setProfesorId(profesorId);
        return cursoRepository.save(curso);
    }

    // Implementation of Asignaturas de Curso
    @Override
    public List<com.proyecto.ColegioBackend.model.CursoAsignatura> listarAsignacionesPorCurso(Long cursoId) {
        List<com.proyecto.ColegioBackend.model.CursoAsignatura> list = cursoAsignaturaRepository.findByCursoId(cursoId);
        if (list.isEmpty()) {
            Curso curso = cursoRepository.findById(cursoId).orElse(null);
            if (curso != null) {
                String suffix = " " + curso.getId();
                cursoAsignaturaRepository.save(com.proyecto.ColegioBackend.model.CursoAsignatura.builder()
                    .curso(curso)
                    .asignatura("ingles" + suffix)
                    .build());
                cursoAsignaturaRepository.save(com.proyecto.ColegioBackend.model.CursoAsignatura.builder()
                    .curso(curso)
                    .asignatura("lenguaje" + suffix)
                    .build());
                cursoAsignaturaRepository.save(com.proyecto.ColegioBackend.model.CursoAsignatura.builder()
                    .curso(curso)
                    .asignatura("matematica" + suffix)
                    .build());
                cursoAsignaturaRepository.save(com.proyecto.ColegioBackend.model.CursoAsignatura.builder()
                    .curso(curso)
                    .asignatura("historia" + suffix)
                    .build());
                list = cursoAsignaturaRepository.findByCursoId(cursoId);
            }
        }
        return list;
    }

    @Override
    @Transactional
    public com.proyecto.ColegioBackend.model.CursoAsignatura asignarProfesorAsignatura(Long cursoId, String asignatura, Long profesorId) {
        // Asegurarse de que las asignaturas están inicializadas
        listarAsignacionesPorCurso(cursoId);
        
        com.proyecto.ColegioBackend.model.CursoAsignatura ca = cursoAsignaturaRepository
            .findByCursoIdAndAsignaturaIgnoreCase(cursoId, asignatura)
            .orElseThrow(() -> new RuntimeException("Asignatura '" + asignatura + "' no encontrada en el curso"));
        
        ca.setProfesorId(profesorId);
        return cursoAsignaturaRepository.save(ca);
    }

    @Override
    public List<com.proyecto.ColegioBackend.model.CursoAsignatura> listarTodasAsignaciones() {
        // Asegurarse de que todos los cursos tienen asignaturas inicializadas
        List<Curso> cursos = cursoRepository.findAll();
        for (Curso c : cursos) {
            listarAsignacionesPorCurso(c.getId());
        }
        return cursoAsignaturaRepository.findAll();
    }

    @Override
    public List<com.proyecto.ColegioBackend.model.CursoAsignatura> listarAsignacionesPorProfesor(Long profesorId) {
        return cursoAsignaturaRepository.findByProfesorId(profesorId);
    }

    @Override
    public com.proyecto.ColegioBackend.model.CursoAsignatura obtenerAsignacionPorCursoYAsignatura(Long cursoId, String asignatura) {
        // Inicializar si es necesario
        listarAsignacionesPorCurso(cursoId);
        return cursoAsignaturaRepository.findByCursoIdAndAsignaturaIgnoreCase(cursoId, asignatura).orElse(null);
    }

    @Override
    public Curso obtenerCursoPorNombre(String nombre) {
        return cursoRepository.findByNombre(nombre).orElse(null);
    }

    // ==================== MATRÍCULAS ====================

    @Override
    @Transactional
    public Matricula matricularAlumno(Long usuarioId, Long cursoId) {
        // Buscar el curso por ID
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException(CURSO_NOT_FOUND_MSG + cursoId));

        // Verificar si ya está matriculado en este curso
        Optional<Matricula> matriculaExistente = matriculaRepository.findByCursoIdAndUsuarioId(curso.getId(), usuarioId);
        if (matriculaExistente.isPresent()) {
            return matriculaExistente.get();
        }

        // Crear la matrícula
        Matricula matricula = Matricula.builder()
                .curso(curso)
                .usuarioId(usuarioId)
                .build();

        return matriculaRepository.save(matricula);
    }

    @Override
    public List<Matricula> listarMatriculasPorCurso(Long cursoId) {
        return matriculaRepository.findByCursoId(cursoId);
    }

    @Override
    public List<Matricula> listarMatriculasPorAlumno(Long usuarioId) {
        return matriculaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional
    public void eliminarMatriculasPorAlumno(Long usuarioId) {
        List<Matricula> matriculas = matriculaRepository.findByUsuarioId(usuarioId);
        if (matriculas != null && !matriculas.isEmpty()) {
            matriculaRepository.deleteAll(matriculas);
        }
    }

    // ==================== PRUEBAS ====================

    @Override
    @Transactional
    public Prueba crearPrueba(Long cursoId, Prueba prueba) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException(CURSO_NOT_FOUND_MSG + cursoId));
        prueba.setCurso(curso);
        return pruebaRepository.save(prueba);
    }

    @Override
    public List<Prueba> listarPruebasPorCurso(Long cursoId) {
        return pruebaRepository.findByCursoId(cursoId);
    }

    @Override
    public Prueba obtenerPruebaPorId(Long id) {
        return pruebaRepository.findById(id).orElse(null);
    }

    // ==================== NOTAS ====================

    @Override
    @Transactional
    public Nota registrarNota(Long pruebaId, Long alumnoId, Double valor, String comentario) {
        if (valor < 1.0 || valor > 7.0) {
            throw new IllegalArgumentException("El valor de la nota debe estar entre 1.0 y 7.0");
        }

        Prueba prueba = pruebaRepository.findById(pruebaId)
                .orElseThrow(() -> new RuntimeException("Prueba no encontrada con ID: " + pruebaId));

        // Verificar si ya existe una nota para este alumno en esta prueba
        Optional<Nota> notaExistente = notaRepository.findByPruebaIdAndAlumnoId(pruebaId, alumnoId);
        
        Nota nota;
        if (notaExistente.isPresent()) {
            nota = notaExistente.get();
            nota.setValor(valor);
            nota.setComentario(comentario);
        } else {
            nota = Nota.builder()
                    .prueba(prueba)
                    .alumnoId(alumnoId)
                    .valor(valor)
                    .comentario(comentario)
                    .build();
        }

        Nota saved = notaRepository.save(nota);

        try {
            NotaGeneradaEvent event = notaGeneradaEventFactory.buildEvent(saved);
            rabbitTemplate.convertAndSend("eventos.exchange", "nota.generada", event);
            logger.info("Publicado evento de nota generada con ID: {}", saved.getId());
        } catch (Exception e) {
            logger.warn("ADVERTENCIA: No se pudo enviar el evento de nota generada a RabbitMQ (¿servidor caído?). Detalle: {}", e.getMessage());
        }

        return saved;
    }

    @Override
    public List<Nota> listarNotasPorPrueba(Long pruebaId) {
        return notaRepository.findByPruebaId(pruebaId);
    }

    @Override
    public List<Nota> listarNotasPorAlumno(Long alumnoId) {
        return notaRepository.findByAlumnoId(alumnoId);
    }
}
