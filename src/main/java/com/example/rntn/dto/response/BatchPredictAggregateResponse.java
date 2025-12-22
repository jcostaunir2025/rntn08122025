package com.example.rntn.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para predicción por lote con análisis agregado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de análisis de sentimiento por lote con estadísticas agregadas")
public class BatchPredictAggregateResponse {

    @Schema(description = "Lista de resultados individuales de análisis")
    private List<AnalisisSentimientoResponse> individualResults;

    @Schema(description = "Estadísticas agregadas del lote")
    private SentimentAggregateStats aggregateAnalysis;

    @Schema(description = "Cantidad de textos procesados", example = "3")
    private Integer processedCount;

    @Schema(description = "Timestamp del análisis")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "IDs de respuestas guardadas en BD (opcional)")
    private List<Long> savedResponseIds;
}

