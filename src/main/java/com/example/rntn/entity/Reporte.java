package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reporte", indexes = {
    @Index(name = "idx_id_usuario", columnList = "id_usuario"),
    @Index(name = "idx_id_evaluacion", columnList = "id_evaluacion"),
    @Index(name = "idx_fechageneracion_reporte", columnList = "fechageneracion_reporte")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Integer idReporte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private Evaluacion evaluacion;

    @Column(name = "fechageneracion_reporte", nullable = false)
    @Builder.Default
    private LocalDateTime fechageneracionReporte = LocalDateTime.now();

    @Column(name = "nombre_reporte", nullable = false, length = 100)
    private String nombreReporte;

    @Column(name = "resultado_reporte", columnDefinition = "TEXT")
    private String resultadoReporte;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

