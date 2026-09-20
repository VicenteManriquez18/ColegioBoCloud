package com.example.servicio_matricula.tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.ColegioBackend.model.Estudiante;
import com.proyecto.ColegioBackend.repository.EstudianteRepository;
import com.proyecto.ColegioBackend.services.MatriculaServiceImpl;
import com.proyecto.ColegioBackend.repository.ApoderadoRepository;
import com.proyecto.ColegioBackend.model.Apoderado;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.mockito.Spy;
import com.proyecto.ColegioBackend.factory.MatriculaRegistradaEventFactory;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
public class MatriculaTests {

    @Mock private EstudianteRepository estudianteRepository;
    @Mock private ApoderadoRepository apoderadoRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    @Spy
    private MatriculaRegistradaEventFactory matriculaRegistradaEventFactory = new MatriculaRegistradaEventFactory();

    @InjectMocks private MatriculaServiceImpl matriculaService;

    private Estudiante estudiante;
    private Apoderado apoderado;

    @BeforeEach
    void setUp() {
        apoderado = new Apoderado();
        apoderado.setId(1L);
        apoderado.setNombre("Juan Perez");
        apoderado.setRut("12.345.678-9");
        apoderado.setUsuarioId(20L);

        estudiante = new Estudiante();
        estudiante.setId(1L);
        estudiante.setNombre("Diego Perez");
        estudiante.setRut("23.456.789-0");
        estudiante.setApoderado(apoderado);
        estudiante.setEstado("Activo");
        estudiante.setUsuarioId(10L);

        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void listarTodosEstudiantes_DebeRetornarLista() {
        when(estudianteRepository.findAll()).thenReturn(Arrays.asList(estudiante));

        List<Estudiante> resultado = matriculaService.listarTodosEstudiantes();

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(estudianteRepository, times(1)).findAll();
    }

    @Test
    void listarTodosEstudiantes_CuandoHayError_DebeRetornarListaVacia() {
        when(estudianteRepository.findAll()).thenThrow(new RuntimeException("Error de conexión"));

        List<Estudiante> resultado = matriculaService.listarTodosEstudiantes();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorId_CuandoExiste_DebeRetornarEstudiante() {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));

        Estudiante resultado = matriculaService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeRetornarNull() {
        when(estudianteRepository.findById(99L)).thenReturn(Optional.empty());

        Estudiante resultado = matriculaService.buscarPorId(99L);

        assertNull(resultado);
    }

    @Test
    void buscarEstudiantePorRut_CuandoExiste_DebeRetornarEstudiante() {
        when(estudianteRepository.findByRut("23.456.789-0")).thenReturn(Optional.of(estudiante));

        Estudiante resultado = matriculaService.buscarEstudiantePorRut("23.456.789-0");

        assertNotNull(resultado);
        assertEquals("23.456.789-0", resultado.getRut());
    }

    @Test
    void registrarMatriculaCompleta_CuandoApoderadoEsNuevo_DebeGuardarAmbos() {
        // Mock role checking
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(Map.of("rol", "Alumno")))
                .thenReturn(Mono.just(Map.of("rol", "Apoderado")));

        // Simulamos que el apoderado NO existe en la BD
        when(apoderadoRepository.findByRut(any())).thenReturn(Optional.empty());
        when(apoderadoRepository.save(any())).thenReturn(apoderado);
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(estudiante);

        Estudiante resultado = matriculaService.registrarMatriculaCompleta(estudiante);

        assertNotNull(resultado);
        verify(apoderadoRepository, times(1)).save(any(Apoderado.class));
        verify(estudianteRepository, times(1)).save(estudiante);
    }

    @Test
    void registrarMatriculaCompleta_CuandoApoderadoYaExiste_DebeReutilizarlo() {
        // Mock role checking
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(Map.of("rol", "Alumno")))
                .thenReturn(Mono.just(Map.of("rol", "Apoderado")));

        // Simulamos que el apoderado SI existe
        when(apoderadoRepository.findByRut(apoderado.getRut())).thenReturn(Optional.of(apoderado));
        when(apoderadoRepository.save(any(Apoderado.class))).thenReturn(apoderado);
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(estudiante);

        Estudiante resultado = matriculaService.registrarMatriculaCompleta(estudiante);

        assertNotNull(resultado);
        assertNotNull(resultado.getApoderado());
        assertEquals(apoderado.getId(), resultado.getApoderado().getId());
        // Verificamos que se llamó al save de apoderado 1 vez para actualizar campos
        verify(apoderadoRepository, times(1)).save(any(Apoderado.class));
        verify(estudianteRepository).save(estudiante);
    }
}
