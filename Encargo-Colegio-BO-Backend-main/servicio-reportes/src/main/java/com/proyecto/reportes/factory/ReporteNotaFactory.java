package com.proyecto.reportes.factory;

import com.proyecto.reportes.entity.ReporteNota;
import com.proyecto.reportes.model.event.NotaGeneradaEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class ReporteNotaFactory implements ReporteFactory<NotaGeneradaEvent, ReporteNota> {

    @Override
    public ReporteNota buildReportEntity(NotaGeneradaEvent event) {
        LocalDateTime fechaEvento = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(event.getFechaEvento()),
                ZoneId.systemDefault()
        );
        return new ReporteNota(
                event.getNotaId(),
                event.getPruebaId(),
                event.getAlumnoId(),
                event.getValor(),
                LocalDateTime.now(),
                fechaEvento
        );
    }
}
