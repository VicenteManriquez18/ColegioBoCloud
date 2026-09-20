package com.proyecto.ColegioBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notas")
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prueba_id", nullable = false)
    private Prueba prueba;

    // Vincula al alumno (Usuario) desde servicio-usuarios
    @Column(name = "alumno_id", nullable = false)
    private Long alumnoId;

    @Column(nullable = false)
    private Double valor;

    private String comentario;
}
