package com.proyecto.mensajeria.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long remitenteId;

    @Column(nullable = false)
    private String remitenteNombre;

    @Column(nullable = false)
    private String remitenteRol;

    @Column(nullable = false)
    private Long destinatarioId;

    @Column(nullable = false)
    private String destinatarioNombre;

    @Column(nullable = false)
    private String destinatarioRol;

    @Column(nullable = false, length = 1000)
    private String contenido;

    @Column(nullable = false)
    private LocalDateTime fechaEnvio;
}
