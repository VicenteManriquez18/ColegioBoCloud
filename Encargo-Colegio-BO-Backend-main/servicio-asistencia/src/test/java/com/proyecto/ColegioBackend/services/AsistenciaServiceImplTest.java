package com.proyecto.ColegioBackend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

import com.proyecto.ColegioBackend.model.Asistencia;
import com.proyecto.ColegioBackend.repository.AsistenciaRepository;
import org.mockito.Spy;
import com.proyecto.ColegioBackend.factory.AsistenciaRegistradaEventFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
public class AsistenciaServiceImplTest {

    @Mock
    private AsistenciaRepository asistenciaRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Spy
    private AsistenciaRegistradaEventFactory asistenciaRegistradaEventFactory = new AsistenciaRegistradaEventFactory();

    @InjectMocks
    private AsistenciaServiceImpl asistenciaService;

    private Asistencia asistencia;

    @BeforeEach
    void setUp() {
        asistencia = new Asistencia();
    }

    @Test
    void listarTodas_Exito() {
        // Arrange
        List<Asistencia> listaEsperada = Arrays.asList(new Asistencia(), new Asistencia());
        when(asistenciaRepository.findAll()).thenReturn(listaEsperada);

        // Act
        List<Asistencia> resultado = asistenciaService.listarTodas();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(asistenciaRepository, times(1)).findAll();
    }

    @Test
    void listarTodas_Excepcion() {
        // Arrange
        when(asistenciaRepository.findAll()).thenThrow(new RuntimeException("Error simulado de base de datos"));

        // Act
        List<Asistencia> resultado = asistenciaService.listarTodas();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(asistenciaRepository, times(1)).findAll();
    }

    @Test
    void guardar_Exito() {
        // Arrange
        when(asistenciaRepository.save(any(Asistencia.class))).thenReturn(asistencia);

        // Act
        Asistencia resultado = asistenciaService.guardar(asistencia);

        // Assert
        assertNotNull(resultado);
        verify(asistenciaRepository, times(1)).save(asistencia);
    }

    @Test
    void guardar_Excepcion() {
        // Arrange
        when(asistenciaRepository.save(any(Asistencia.class)))
                .thenThrow(new RuntimeException("Error simulado al guardar"));

        // Act
        Asistencia resultado = asistenciaService.guardar(asistencia);

        // Assert
        assertNull(resultado);
        verify(asistenciaRepository, times(1)).save(asistencia);
    }

    @Test
    void buscarPorId_Encontrado() {
        // Arrange
        Long id = 1L;
        when(asistenciaRepository.findById(id)).thenReturn(Optional.of(asistencia));

        // Act
        Asistencia resultado = asistenciaService.buscarPorId(id);

        // Assert
        assertNotNull(resultado);
        verify(asistenciaRepository, times(1)).findById(id);
    }

    @Test
    void buscarPorId_NoEncontrado() {
        // Arrange
        Long id = 1L;
        when(asistenciaRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Asistencia resultado = asistenciaService.buscarPorId(id);

        // Assert
        assertNull(resultado);
        verify(asistenciaRepository, times(1)).findById(id);
    }

    @Test
    void buscarPorId_Excepcion() {
        // Arrange
        Long id = 1L;
        when(asistenciaRepository.findById(id)).thenThrow(new RuntimeException("Error simulado al buscar por ID"));

        // Act
        Asistencia resultado = asistenciaService.buscarPorId(id);

        // Assert
        assertNull(resultado);
        verify(asistenciaRepository, times(1)).findById(id);
    }

    @Test
    void eliminar_Exito() {
        // Arrange
        Long id = 1L;
        doNothing().when(asistenciaRepository).deleteById(id);

        // Act
        asistenciaService.eliminar(id);

        // Assert
        verify(asistenciaRepository, times(1)).deleteById(id);
    }

    @Test
    void eliminar_Excepcion() {
        // Arrange
        Long id = 1L;
        doThrow(new RuntimeException("Error simulado al eliminar")).when(asistenciaRepository).deleteById(id);

        // Act & Assert (verificar que no propaga la excepción debido al try-catch)
        assertDoesNotThrow(() -> asistenciaService.eliminar(id));
        verify(asistenciaRepository, times(1)).deleteById(id);
    }
}
