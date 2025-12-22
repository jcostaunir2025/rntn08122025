package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA para Consulta
 * Representa una consulta médica entre un paciente y personal
 */
@Entity
@Table(name = "consulta", indexes = {
    @Index(name = "idx_id_paciente", columnList = "id_paciente"),
    @Index(name = "idx_id_personal", columnList = "id_personal"),
    @Index(name = "idx_fechahora_consulta", columnList = "fechahora_consulta"),
    @Index(name = "idx_estatus_consulta", columnList = "estatus_consulta")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Integer idConsulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_personal", nullable = false)
    private Personal personal;

    @Column(name = "fechahora_consulta", nullable = false)
    private LocalDateTime fechahoraConsulta;

    @Column(name = "fechafin_consulta")
    private LocalDateTime fechafinConsulta;

    @Column(name = "estatus_consulta", nullable = false)
    private Integer estatusConsulta;

    // Relationship to ConsultaEstatus for status name resolution
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estatus_consulta", insertable = false, updatable = false)
    private ConsultaEstatus consultaEstatus;

    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Evaluacion> evaluaciones = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void addEvaluacion(Evaluacion evaluacion) {
        evaluaciones.add(evaluacion);
        evaluacion.setConsulta(this);
    }
}

