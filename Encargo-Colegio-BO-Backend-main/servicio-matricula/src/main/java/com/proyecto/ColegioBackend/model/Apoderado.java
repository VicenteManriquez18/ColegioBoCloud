package com.proyecto.ColegioBackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "apoderados")
@Data
public class Apoderado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String rut;

    private String telefono;

    private String correo;

    private String direccion;

    // Relación con el parentesco (Padre, Madre, Tío, etc.)
    private String parentesco;

    // Vincula al apoderado con su cuenta de login en servicio-usuarios
    private Long usuarioId;

    @OneToMany(mappedBy = "apoderado")
    @JsonIgnore
    private List<Estudiante> estudiantes;
}
