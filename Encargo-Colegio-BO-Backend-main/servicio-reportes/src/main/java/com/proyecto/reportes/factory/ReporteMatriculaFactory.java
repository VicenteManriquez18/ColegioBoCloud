package com.proyecto.reportes.factory;

import com.proyecto.reportes.entity.ReporteMatricula;
import com.proyecto.reportes.model.event.MatriculaRegistradaEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class ReporteMatriculaFactory implements ReporteFactory<MatriculaRegistradaEvent, ReporteMatricula> {

    @Override
    public ReporteMatricula buildReportEntity(MatriculaRegistradaEvent event) {
        LocalDateTime fechaEvento = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(event.getFechaEvento()),
                ZoneId.systemDefault()
        );
        return new ReporteMatricula(
                event.getMatriculaId(),
                event.getAlumnoId(),
                event.getCursoId(),
                event.getEstado(),
                LocalDateTime.now(),
                fechaEvento
        );
    }
}
