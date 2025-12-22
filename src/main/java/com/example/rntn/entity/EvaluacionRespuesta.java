package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA para EvaluacionRespuesta
 * ⭐ INTEGRACIÓN RNTN: Almacena respuestas con análisis de sentimiento
 */
@Entity
@Table(name = "evaluacion_respuesta", indexes = {
    @Index(name = "idx_id_evaluacion_pregunta", columnList = "id_evaluacion_pregunta"),
    @Index(name = "idx_label_evaluacion_respuesta", columnList = "label_evaluacion_respuesta")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion_respuesta")
    private Integer idEvaluacionRespuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion_pregunta", nullable = false)
    private EvaluacionPregunta evaluacionPregunta;

    @Column(name = "texto_evaluacion_respuesta", nullable = false, columnDefinition = "TEXT")
    private String textoEvaluacionRespuesta;

    @Column(name = "texto_set_evaluacion_respuesta", columnDefinition = "TEXT")
    private String textoSetEvaluacionRespuesta;

    /**
     * ⭐ Label predicho por el modelo RNTN
     * Valores: ANXIETY, SUICIDAL, ANGER, SADNESS, FRUSTRATION
     */
    @Column(name = "label_evaluacion_respuesta", length = 50)
    private String labelEvaluacionRespuesta;

    /**
     * ⭐ Score de confianza del modelo RNTN (0.0 - 1.0)
     */
    @Column(name = "confidence_score")
    private Double confidenceScore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

