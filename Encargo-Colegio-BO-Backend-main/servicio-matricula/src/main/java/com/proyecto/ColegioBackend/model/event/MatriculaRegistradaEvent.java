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
public class MatriculaRegistradaEvent {

    @JsonProperty("matricula_id")
    private Long matriculaId;

    @JsonProperty("alumno_id")
    private Long alumnoId;

    @JsonProperty("curso_id")
    private Long cursoId;

    @JsonProperty("fecha_evento")
    private Long fechaEvento;

    @JsonProperty("estado")
    private String estado;
}
