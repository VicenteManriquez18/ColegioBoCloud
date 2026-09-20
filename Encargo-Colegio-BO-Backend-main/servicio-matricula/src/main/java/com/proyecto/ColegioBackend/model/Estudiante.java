package com.proyecto.ColegioBackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "estudiantes")
@Data
public class Estudiante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String rut;

    private LocalDate fechaNacimiento;
    private Long cursoId;

    // Este ID vincula al estudiante con su cuenta de login en servicio-usuarios
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "apoderado_id")
    private Apoderado apoderado;

    private String estado; // Activo, Retirado, etc.
}