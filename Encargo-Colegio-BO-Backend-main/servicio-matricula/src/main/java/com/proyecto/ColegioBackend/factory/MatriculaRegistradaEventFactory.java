package com.proyecto.ColegioBackend.factory;

import com.proyecto.ColegioBackend.model.Estudiante;
import com.proyecto.ColegioBackend.model.event.MatriculaRegistradaEvent;
import org.springframework.stereotype.Component;

@Component
public class MatriculaRegistradaEventFactory implements EventFactory<Estudiante, MatriculaRegistradaEvent> {

    @Override
    public MatriculaRegistradaEvent buildEvent(Estudiante estudiante) {
        return MatriculaRegistradaEvent.builder()
                .matriculaId(estudiante.getId())
                .alumnoId(estudiante.getUsuarioId())
                .cursoId(estudiante.getCursoId())
                .fechaEvento(System.currentTimeMillis())
                .estado(estudiante.getEstado())
                .build();
    }
}
