package com.example.rntn.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para EvaluacionRespuesta con análisis de sentimiento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de evaluación con análisis de sentimiento integrado")
public class EvaluacionRespuestaResponse {

    @Schema(description = "ID de la respuesta", example = "1")
    private Integer idEvaluacionRespuesta;

    @Schema(description = "ID de la pregunta", example = "1")
    private Integer idEvaluacionPregunta;

    @Schema(description = "Texto de la pregunta", example = "¿Cómo se siente hoy?")
    private String textoPregunta;

    @Schema(description = "Texto de la respuesta del paciente")
    private String textoEvaluacionRespuesta;

    @Schema(description = "Texto normalizado/procesado")
    private String textoSetEvaluacionRespuesta;

    @Schema(description = "Label del sentimiento detectado", example = "ANXIETY")
    private String labelEvaluacionRespuesta;

    @Schema(description = "Score de confianza del modelo", example = "0.92")
    private Double confidenceScore;

    @Schema(description = "Análisis detallado del sentimiento")
    private AnalisisSentimientoResponse sentimentAnalysis;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación", example = "2025-12-21T15:30:00")
    private LocalDateTime createdAt;
}

