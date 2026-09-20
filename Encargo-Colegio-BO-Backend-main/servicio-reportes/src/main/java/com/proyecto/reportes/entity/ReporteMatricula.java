package com.proyecto.reportes.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reporte_matriculas")
public class ReporteMatricula implements ReporteEntity {
    
    @Override
    public LocalDateTime getFechaProcesamiento() {
        return this.fechaRegistro;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "matricula_id", nullable = false)
    private Long matriculaId;
    
    @Column(name = "alumno_id", nullable = false)
    private Long alumnoId;
    
    @Column(name = "curso_id", nullable = false)
    private Long cursoId;
    
    @Column(name = "estado")
    private String estado;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    @Column(name = "fecha_evento")
    private LocalDateTime fechaEvento;

    public ReporteMatricula() {}

    public ReporteMatricula(Long matriculaId, Long alumnoId, Long cursoId, String estado, LocalDateTime fechaRegistro, LocalDateTime fechaEvento) {
        this.matriculaId = matriculaId;
        this.alumnoId = alumnoId;
        this.cursoId = cursoId;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
        this.fechaEvento = fechaEvento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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
