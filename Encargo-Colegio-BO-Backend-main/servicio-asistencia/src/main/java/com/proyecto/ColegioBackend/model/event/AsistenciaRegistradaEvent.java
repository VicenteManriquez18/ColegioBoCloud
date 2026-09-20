package com.proyecto.ColegioBackend.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaRegistradaEvent {

    @JsonProperty("asistencia_id")
    private Long asistenciaId;

    @JsonProperty("alumno_id")
    private Long alumnoId;

    @JsonProperty("curso_id")
    private Long cursoId;

    @JsonProperty("presente")
    private Boolean presente;

    @JsonProperty("fecha_evento")
    private Long fechaEvento;
}
