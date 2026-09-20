package com.proyecto.reportes.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reporte_notas")
public class ReporteNota implements ReporteEntity {
    
    @Override
    public LocalDateTime getFechaProcesamiento() {
        return this.fechaRegistro;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nota_id", nullable = false)
    private Long notaId;
    
    @Column(name = "prueba_id", nullable = false)
    private Long pruebaId;
    
    @Column(name = "alumno_id", nullable = false)
    private Long alumnoId;
    
    @Column(name = "valor", nullable = false)
    private Double valor;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    @Column(name = "fecha_evento")
    private LocalDateTime fechaEvento;

    public ReporteNota() {}

    public ReporteNota(Long notaId, Long pruebaId, Long alumnoId, Double valor, LocalDateTime fechaRegistro, LocalDateTime fechaEvento) {
        this.notaId = notaId;
        this.pruebaId = pruebaId;
        this.alumnoId = alumnoId;
        this.valor = valor;
        this.fechaRegistro = fechaRegistro;
        this.fechaEvento = fechaEvento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
