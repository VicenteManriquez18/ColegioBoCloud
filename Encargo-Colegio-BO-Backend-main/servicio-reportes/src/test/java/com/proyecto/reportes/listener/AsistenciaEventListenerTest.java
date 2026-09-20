package com.proyecto.reportes.listener;

import com.proyecto.reportes.entity.ReporteAsistencia;
import com.proyecto.reportes.factory.ReporteAsistenciaFactory;
import com.proyecto.reportes.model.event.AsistenciaRegistradaEvent;
import com.proyecto.reportes.repository.ReporteAsistenciaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AsistenciaEventListenerTest {

    @Mock
    private ReporteAsistenciaRepository repository;

    @Spy
    private ReporteAsistenciaFactory factory = new ReporteAsistenciaFactory();

    @InjectMocks
    private AsistenciaEventListener listener;

    @Test
    public void testHandleAsistenciaRegistrada() {
        AsistenciaRegistradaEvent event = new AsistenciaRegistradaEvent(1L, 2L, 3L, true, System.currentTimeMillis());

        listener.handleAsistenciaRegistrada(event);

        verify(factory, times(1)).buildReportEntity(event);
        verify(repository, times(1)).save(any(ReporteAsistencia.class));
    }
}
