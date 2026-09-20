package com.proyecto.ColegioBackend.factory;

import com.proyecto.ColegioBackend.model.Asistencia;
import com.proyecto.ColegioBackend.model.event.AsistenciaRegistradaEvent;
import org.springframework.stereotype.Component;

@Component
public class AsistenciaRegistradaEventFactory implements EventFactory<Asistencia, AsistenciaRegistradaEvent> {

    @Override
    public AsistenciaRegistradaEvent buildEvent(Asistencia asistencia) {
        return AsistenciaRegistradaEvent.builder()
                .asistenciaId(asistencia.getId())
                .alumnoId(asistencia.getUsuarioId())
                .cursoId(asistencia.getCursoId())
                .presente(asistencia.getPresente())
                .fechaEvento(System.currentTimeMillis())
                .build();
    }
}
