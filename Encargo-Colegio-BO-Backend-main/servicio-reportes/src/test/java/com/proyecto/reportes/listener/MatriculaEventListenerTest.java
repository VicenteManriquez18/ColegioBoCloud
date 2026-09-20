package com.proyecto.reportes.listener;

import com.proyecto.reportes.entity.ReporteMatricula;
import com.proyecto.reportes.factory.ReporteMatriculaFactory;
import com.proyecto.reportes.model.event.MatriculaRegistradaEvent;
import com.proyecto.reportes.repository.ReporteMatriculaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatriculaEventListenerTest {

    @Mock
    private ReporteMatriculaRepository repository;

    @Spy
    private ReporteMatriculaFactory factory = new ReporteMatriculaFactory();

    @InjectMocks
    private MatriculaEventListener listener;

    @Test
    public void testHandleMatriculaRegistrada() {
        MatriculaRegistradaEvent event = new MatriculaRegistradaEvent(1L, 2L, 3L, System.currentTimeMillis(), "COMPLETED");

        listener.handleMatriculaRegistrada(event);

        verify(factory, times(1)).buildReportEntity(event);
        verify(repository, times(1)).save(any(ReporteMatricula.class));
    }
}
