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
public class NotaGeneradaEvent {

    @JsonProperty("nota_id")
    private Long notaId;

    @JsonProperty("prueba_id")
    private Long pruebaId;

    @JsonProperty("alumno_id")
    private Long alumnoId;

    @JsonProperty("valor")
    private Double valor;

    @JsonProperty("fecha_evento")
    private Long fechaEvento;
}
