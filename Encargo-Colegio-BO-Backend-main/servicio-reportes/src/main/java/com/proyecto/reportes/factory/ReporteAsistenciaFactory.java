package com.proyecto.reportes.factory;

import com.proyecto.reportes.entity.ReporteAsistencia;
import com.proyecto.reportes.model.event.AsistenciaRegistradaEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class ReporteAsistenciaFactory implements ReporteFactory<AsistenciaRegistradaEvent, ReporteAsistencia> {

    @Override
    public ReporteAsistencia buildReportEntity(AsistenciaRegistradaEvent event) {
        LocalDateTime fechaEvento = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(event.getFechaEvento()),
                ZoneId.systemDefault()
        );
        return new ReporteAsistencia(
                event.getAsistenciaId(),
                event.getAlumnoId(),
                event.getCursoId(),
                event.getPresente(),
                LocalDateTime.now(),
                fechaEvento
        );
    }
}
