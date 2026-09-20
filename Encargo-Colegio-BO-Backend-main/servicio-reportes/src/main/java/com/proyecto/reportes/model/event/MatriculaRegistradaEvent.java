package com.proyecto.reportes.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public MatriculaRegistradaEvent() {}

    public MatriculaRegistradaEvent(Long matriculaId, Long alumnoId, Long cursoId, Long fechaEvento, String estado) {
        this.matriculaId = matriculaId;
        this.alumnoId = alumnoId;
        this.cursoId = cursoId;
        this.fechaEvento = fechaEvento;
        this.estado = estado;
    }

    public Long getMatriculaId() {
        return matriculaId;
    }

    public void setMatriculaId(Long matriculaId) {
        this.matriculaId = matriculaId;
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

    public Long getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(Long fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
