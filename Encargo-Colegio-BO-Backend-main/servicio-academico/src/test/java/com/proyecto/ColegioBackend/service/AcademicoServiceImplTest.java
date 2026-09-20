package com.proyecto.ColegioBackend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;
import com.proyecto.ColegioBackend.factory.NotaGeneradaEventFactory;

import com.proyecto.ColegioBackend.model.Curso;
import com.proyecto.ColegioBackend.model.Matricula;
import com.proyecto.ColegioBackend.model.Prueba;
import com.proyecto.ColegioBackend.model.Nota;
import com.proyecto.ColegioBackend.repository.CursoRepository;
import com.proyecto.ColegioBackend.repository.MatriculaRepository;
import com.proyecto.ColegioBackend.repository.PruebaRepository;
import com.proyecto.ColegioBackend.repository.NotaRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
public class AcademicoServiceImplTest {
    @Mock
    private CursoRepository cursoRepository;
    @Mock
    private MatriculaRepository matriculaRepository;
    @Mock
    private PruebaRepository pruebaRepository;
    @Mock
    private NotaRepository notaRepository;
    @Mock
    private com.proyecto.ColegioBackend.repository.CursoAsignaturaRepository cursoAsignaturaRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Spy
    private NotaGeneradaEventFactory notaGeneradaEventFactory = new NotaGeneradaEventFactory();
    @InjectMocks
    private AcademicoServiceImpl academicoService;
    private Curso curso;
    private Matricula matricula;
    private Prueba prueba;
    private Nota nota;

    @BeforeEach
    void setUp() {
        curso = Curso.builder()
                .id(1L)
                .nombre("1 A")
                .codigo("CUR-1-A")
                .descripcion("Primer Año A")
                .profesorId(10L)
                .build();
        matricula = Matricula.builder()
                .id(1L)
                .curso(curso)
                .usuarioId(5L)
                .build();
        prueba = Prueba.builder()
                .id(1L)
                .titulo("Control 1")
                .descripcion("Prueba de Matemáticas")
                .fecha(LocalDate.now())
                .curso(curso)
                .build();
        nota = Nota.builder()
                .id(1L)
                .prueba(prueba)
                .alumnoId(5L)
                .valor(6.5)
                .comentario("Buen desempeño")
                .build();
    }

    // ==================== CURSOS ====================

    @Test
    void listarCursos_Exito() {
        when(cursoRepository.findAll()).thenReturn(Arrays.asList(curso));
        List<Curso> resultado = academicoService.listarCursos();
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cursoRepository, times(1)).findAll();
    }

    @Test
    void obtenerCursoPorId_Encontrado() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        Curso resultado = academicoService.obtenerCursoPorId(1L);
        assertNotNull(resultado);
        assertEquals("1 A", resultado.getNombre());
        verify(cursoRepository, times(1)).findById(1L);
    }

    @Test
    void crearCurso_Exito() {
        when(cursoRepository.save(any(Curso.class))).thenReturn(curso);
        when(cursoAsignaturaRepository.save(any(com.proyecto.ColegioBackend.model.CursoAsignatura.class)))
                .thenReturn(null);
        Curso nuevoCurso = Curso.builder().nombre("2 B").build();
        Curso resultado = academicoService.crearCurso(nuevoCurso);
        assertNotNull(resultado);
        verify(cursoRepository, times(1)).save(any(Curso.class));
        verify(cursoAsignaturaRepository, times(4)).save(any(com.proyecto.ColegioBackend.model.CursoAsignatura.class));
    }

    @Test
    void asignarProfesor_Exito() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.save(any(Curso.class))).thenReturn(curso);

        Curso resultado = academicoService.asignarProfesor(1L, 20L);
        assertNotNull(resultado);
        verify(cursoRepository, times(1)).findById(1L);
        verify(cursoRepository, times(1)).save(curso);
    }

    // ==================== MATRÍCULAS ====================

    @Test
    void matricularAlumno_CursoExisteYNoMatriculado() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(matriculaRepository.findByCursoIdAndUsuarioId(1L, 5L)).thenReturn(Optional.empty());
        when(matriculaRepository.save(any(Matricula.class))).thenReturn(matricula);

        Matricula resultado = academicoService.matricularAlumno(5L, 1L);
        assertNotNull(resultado);
        verify(cursoRepository, times(1)).findById(1L);
        verify(matriculaRepository, times(1)).findByCursoIdAndUsuarioId(1L, 5L);
        verify(matriculaRepository, times(1)).save(any(Matricula.class));
    }

    @Test
    void matricularAlumno_YaMatriculado() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(matriculaRepository.findByCursoIdAndUsuarioId(1L, 5L)).thenReturn(Optional.of(matricula));

        Matricula resultado = academicoService.matricularAlumno(5L, 1L);
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(matriculaRepository, never()).save(any(Matricula.class));
    }

    // ==================== PRUEBAS ====================

    @Test
    void crearPrueba_Exito() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(pruebaRepository.save(any(Prueba.class))).thenReturn(prueba);

        Prueba resultado = academicoService.crearPrueba(1L, prueba);
        assertNotNull(resultado);
        verify(pruebaRepository, times(1)).save(prueba);
    }

    // ==================== NOTAS ====================

    @Test
    void registrarNota_NuevaNotaExito() {
        when(pruebaRepository.findById(1L)).thenReturn(Optional.of(prueba));
        when(notaRepository.findByPruebaIdAndAlumnoId(1L, 5L)).thenReturn(Optional.empty());
        when(notaRepository.save(any(Nota.class))).thenReturn(nota);

        Nota resultado = academicoService.registrarNota(1L, 5L, 6.5, "Muy bien");
        assertNotNull(resultado);
        verify(notaRepository, times(1)).save(any(Nota.class));
    }

    @Test
    void registrarNota_ActualizarNotaExito() {
        when(pruebaRepository.findById(1L)).thenReturn(Optional.of(prueba));
        when(notaRepository.findByPruebaIdAndAlumnoId(1L, 5L)).thenReturn(Optional.of(nota));
        when(notaRepository.save(any(Nota.class))).thenReturn(nota);

        Nota resultado = academicoService.registrarNota(1L, 5L, 7.0, "Excelente");
        assertNotNull(resultado);
        assertEquals(7.0, resultado.getValor());
        assertEquals("Excelente", resultado.getComentario());
        verify(notaRepository, times(1)).save(nota);
    }

    @Test
    void registrarNota_ValorInvalidoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            academicoService.registrarNota(1L, 5L, 7.5, "Invalida");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            academicoService.registrarNota(1L, 5L, 0.9, "Invalida");
        });
        verify(notaRepository, never()).save(any(Nota.class));
    }
}
