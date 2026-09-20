package com.proyecto.reportes.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public AsistenciaRegistradaEvent() {}

    public AsistenciaRegistradaEvent(Long asistenciaId, Long alumnoId, Long cursoId, Boolean presente, Long fechaEvento) {
        this.asistenciaId = asistenciaId;
        this.alumnoId = alumnoId;
        this.cursoId = cursoId;
        this.presente = presente;
        this.fechaEvento = fechaEvento;
    }

    public Long getAsistenciaId() {
        return asistenciaId;
    }

    public void setAsistenciaId(Long asistenciaId) {
        this.asistenciaId = asistenciaId;
    }

    public Long getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public Boolean getPresente() {
        return presente;
    }

    public void setPresente(Boolean presente) {
        this.presente = presente;
    }

    public Long getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(Long fechaEvento) {
        this.fechaEvento = fechaEvento;
    }
}
