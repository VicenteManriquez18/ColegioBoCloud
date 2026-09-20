package com.proyecto.reportes.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public NotaGeneradaEvent() {}

    public NotaGeneradaEvent(Long notaId, Long pruebaId, Long alumnoId, Double valor, Long fechaEvento) {
        this.notaId = notaId;
        this.pruebaId = pruebaId;
        this.alumnoId = alumnoId;
        this.valor = valor;
        this.fechaEvento = fechaEvento;
    }

    public Long getNotaId() {
        return notaId;
    }

    public void setNotaId(Long notaId) {
        this.notaId = notaId;
    }

    public Long getPruebaId() {
        return pruebaId;
    }

    public void setPruebaId(Long pruebaId) {
        this.pruebaId = pruebaId;
    }

    public Long getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public Long getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(Long fechaEvento) {
        this.fechaEvento = fechaEvento;
    }
}
