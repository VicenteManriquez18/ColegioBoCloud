package com.proyecto.reportes.dto;

import com.proyecto.reportes.entity.ReporteComportamiento;
import java.util.List;
import java.util.Map;

public class ReporteCompletoDTO {
    private Long alumnoId;
    private List<Map<String, Object>> notas;
    private List<Map<String, Object>> asistencias;
    private List<ReporteComportamiento> comportamientos;
    private Double promedioGeneral;
    private Double porcentajeAsistencia; // Usaremos la cantidad de asistencias marcadas para este campo

    // Manual no-args constructor
    public ReporteCompletoDTO() {}

    // Manual all-args constructor
    public ReporteCompletoDTO(Long alumnoId, List<Map<String, Object>> notas, List<Map<String, Object>> asistencias, List<ReporteComportamiento> comportamientos, Double promedioGeneral, Double porcentajeAsistencia) {
        this.alumnoId = alumnoId;
        this.notas = notas;
        this.asistencias = asistencias;
        this.comportamientos = comportamientos;
        this.promedioGeneral = promedioGeneral;
        this.porcentajeAsistencia = porcentajeAsistencia;
    }

    // Manual Getters and Setters
    public Long getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }

    public List<Map<String, Object>> getNotas() {
        return notas;
    }

    public void setNotas(List<Map<String, Object>> notas) {
        this.notas = notas;
    }

    public List<Map<String, Object>> getAsistencias() {
        return asistencias;
    }

    public void setAsistencias(List<Map<String, Object>> asistencias) {
        this.asistencias = asistencias;
    }

    public List<ReporteComportamiento> getComportamientos() {
        return comportamientos;
    }

    public void setComportamientos(List<ReporteComportamiento> comportamientos) {
        this.comportamientos = comportamientos;
    }

    public Double getPromedioGeneral() {
        return promedioGeneral;
    }

    public void setPromedioGeneral(Double promedioGeneral) {
        this.promedioGeneral = promedioGeneral;
    }

    public Double getPorcentajeAsistencia() {
        return porcentajeAsistencia;
    }

    public void setPorcentajeAsistencia(Double porcentajeAsistencia) {
        this.porcentajeAsistencia = porcentajeAsistencia;
    }
}
