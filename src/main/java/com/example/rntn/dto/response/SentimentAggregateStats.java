package com.example.rntn.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para estadísticas agregadas de análisis de sentimientos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estadísticas agregadas de análisis de sentimientos")
public class SentimentAggregateStats {

    @Schema(description = "Distribución de sentimientos por label")
    private Map<String, Integer> sentimentDistribution;

    @Schema(description = "Sentimiento dominante (más frecuente)", example = "ANXIETY")
    private String dominantSentiment;

    @Schema(description = "Confianza promedio (0.0 - 1.0)", example = "0.887")
    private Double averageConfidence;

    @Schema(description = "Confianza mínima", example = "0.75")
    private Double minConfidence;

    @Schema(description = "Confianza máxima", example = "0.95")
    private Double maxConfidence;

    @Schema(description = "Nivel de riesgo más alto detectado", example = "MEDIO",
            allowableValues = {"BAJO", "MEDIO", "ALTO"})
    private String highestRiskLevel;

    @Schema(description = "Número de alertas de alto riesgo detectadas", example = "1")
    private Integer highRiskAlerts;

    @Schema(description = "Total de respuestas analizadas", example = "10")
    private Integer totalResponses;
}

