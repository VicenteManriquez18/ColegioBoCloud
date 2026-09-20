package com.proyecto.reportes.listener;

import com.proyecto.reportes.entity.ReporteNota;
import com.proyecto.reportes.factory.ReporteNotaFactory;
import com.proyecto.reportes.model.event.NotaGeneradaEvent;
import com.proyecto.reportes.repository.ReporteNotaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotaEventListenerTest {

    @Mock
    private ReporteNotaRepository repository;

    @Spy
    private ReporteNotaFactory factory = new ReporteNotaFactory();

    @InjectMocks
    private NotaEventListener listener;

    @Test
    public void testHandleNotaGenerada() {
        NotaGeneradaEvent event = new NotaGeneradaEvent(1L, 2L, 3L, 6.5, System.currentTimeMillis());

        listener.handleNotaGenerada(event);

        verify(factory, times(1)).buildReportEntity(event);
        verify(repository, times(1)).save(any(ReporteNota.class));
    }
}
