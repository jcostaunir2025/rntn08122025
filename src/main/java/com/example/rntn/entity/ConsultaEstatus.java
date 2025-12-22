package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA para ConsultaEstatus
 * Catálogo de estados de consulta
 */
@Entity
@Table(name = "consulta_estatus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaEstatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta_estatus")
    private Integer idConsultaEstatus;

    @Column(name = "nombre_consulta_estatus", unique = true, nullable = false, length = 50)
    private String nombreConsultaEstatus;

    // Constants for status IDs
    public static final Integer PENDIENTE = 1;
    public static final Integer EN_PROGRESO = 2;
    public static final Integer COMPLETADA = 3;
    public static final Integer CANCELADA = 4;
    public static final Integer REPROGRAMADA = 5;
    public static final Integer NO_ASISTIO = 6;
}

