package com.proyecto.ColegioBackend.factory;

import com.proyecto.ColegioBackend.model.Nota;
import com.proyecto.ColegioBackend.model.event.NotaGeneradaEvent;
import org.springframework.stereotype.Component;

@Component
public class NotaGeneradaEventFactory implements EventFactory<Nota, NotaGeneradaEvent> {

    @Override
    public NotaGeneradaEvent buildEvent(Nota nota) {
        return NotaGeneradaEvent.builder()
                .notaId(nota.getId())
                .pruebaId(nota.getPrueba().getId())
                .alumnoId(nota.getAlumnoId())
                .valor(nota.getValor())
                .fechaEvento(System.currentTimeMillis())
                .build();
    }
}
