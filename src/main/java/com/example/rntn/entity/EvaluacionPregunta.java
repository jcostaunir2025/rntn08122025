package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluacion_pregunta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionPregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion_pregunta")
    private Integer idEvaluacionPregunta;

    @Column(name = "texto_evaluacion_pregunta", nullable = false, columnDefinition = "TEXT")
    private String textoEvaluacionPregunta;

    @OneToMany(mappedBy = "evaluacionPregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EvaluacionRespuesta> respuestas = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

