package com.proyecto.reportes.service;

import com.proyecto.reportes.dto.ReporteCompletoDTO;
import com.proyecto.reportes.entity.ReporteComportamiento;
import com.proyecto.reportes.repository.ReporteComportamientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReporteCompletoServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private ReporteComportamientoRepository comportamientoRepository;

    @InjectMocks
    private ReporteCompletoService reporteCompletoService;

    // WebClient Mock Mocks
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    public void setUp() {
        // Set URLs in the service via reflection since they are injected via @Value
        ReflectionTestUtils.setField(reporteCompletoService, "academicoUrl", "http://127.0.0.1:8084");
        ReflectionTestUtils.setField(reporteCompletoService, "asistenciaUrl", "http://127.0.0.1:8082");

        requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);
    }

    @Test
    public void testObtenerReporteCompleto_ConComportamiento() {
        Long alumnoId = 1L;

        // Mock WebClient get chain
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Mock responses for WebClient calls
        // First WebClient call: Academico notes
        List<Map<String, Object>> mockNotas = new ArrayList<>();
        Map<String, Object> nota1 = new HashMap<>();
        nota1.put("valor", 6.5);
        mockNotas.add(nota1);
        Map<String, Object> nota2 = new HashMap<>();
        nota2.put("valor", "5.5"); // Test string parsing too
        mockNotas.add(nota2);

        // Second WebClient call: Asistencias
        List<Map<String, Object>> mockAsistencias = new ArrayList<>();
        mockAsistencias.add(new HashMap<>());
        mockAsistencias.add(new HashMap<>());

        Mono<List<Map<String, Object>>> monoNotas = Mono.just(mockNotas);
        Mono<List<Map<String, Object>>> monoAsistencias = Mono.just(mockAsistencias);

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(monoNotas) // First call returns notes
                .thenReturn(monoAsistencias); // Second call returns asistencias

        // Mock comportamientos in Repository
        ReporteComportamiento comportamiento = new ReporteComportamiento();
        comportamiento.setAlumnoId(alumnoId);
        comportamiento.setCalificacion("Excelente");
        comportamiento.setObservaciones("Muy buen desempeño");
        when(comportamientoRepository.findByAlumnoId(alumnoId)).thenReturn(List.of(comportamiento));

        // Execute service
        ReporteCompletoDTO reporte = reporteCompletoService.obtenerReporteCompleto(alumnoId, true);

        // Verify
        assertNotNull(reporte);
        assertEquals(alumnoId, reporte.getAlumnoId());
        assertEquals(2, reporte.getNotas().size());
        assertEquals(2, reporte.getAsistencias().size());
        assertEquals(1, reporte.getComportamientos().size());
        assertEquals("Excelente", reporte.getComportamientos().get(0).getCalificacion());

        // Average calculation test ( (6.5 + 5.5) / 2 = 6.0 )
        assertEquals(6.0, reporte.getPromedioGeneral());
        // Attendance count as percentage
        assertEquals(2.0, reporte.getPorcentajeAsistencia());

        verify(comportamientoRepository, times(1)).findByAlumnoId(alumnoId);
    }

    @Test
    public void testObtenerReporteCompleto_SinComportamiento() {
        Long alumnoId = 2L;

        // Mock WebClient get chain
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Mock responses (empty lists)
        Mono<List<Map<String, Object>>> monoEmpty = Mono.just(Collections.emptyList());
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(monoEmpty);

        // Execute service with incluirComportamiento = false
        ReporteCompletoDTO reporte = reporteCompletoService.obtenerReporteCompleto(alumnoId, false);

        // Verify
        assertNotNull(reporte);
        assertEquals(alumnoId, reporte.getAlumnoId());
        assertTrue(reporte.getNotas().isEmpty());
        assertTrue(reporte.getAsistencias().isEmpty());
        assertTrue(reporte.getComportamientos().isEmpty());
        assertEquals(0.0, reporte.getPromedioGeneral());
        assertEquals(0.0, reporte.getPorcentajeAsistencia());

        // Behavior repository must NOT be called
        verify(comportamientoRepository, never()).findByAlumnoId(anyLong());
    }
}
