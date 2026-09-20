




package com.proyecto.reportes.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reporte_asistencias")
public class ReporteAsistencia implements ReporteEntity {
    
    @Override
    public LocalDateTime getFechaProcesamiento() {
        return this.fechaRegistro;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "asistencia_id", nullable = false)
    private Long asistenciaId;
    
    @Column(name = "alumno_id", nullable = false)
    private Long alumnoId;
    
    @Column(name = "curso_id", nullable = false)
    private Long cursoId;
    
    @Column(name = "presente", nullable = false)
    private Boolean presente;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    @Column(name = "fecha_evento")
    private LocalDateTime fechaEvento;

    public ReporteAsistencia() {}

    public ReporteAsistencia(Long asistenciaId, Long alumnoId, Long cursoId, Boolean presente, LocalDateTime fechaRegistro, LocalDateTime fechaEvento) {
        this.asistenciaId = asistenciaId;
        this.alumnoId = alumnoId;
        this.cursoId = cursoId;
        this.presente = presente;
        this.fechaRegistro = fechaRegistro;
        this.fechaEvento = fechaEvento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDateTime fechaEvento) {
        this.fechaEvento = fechaEvento;
    }
}
