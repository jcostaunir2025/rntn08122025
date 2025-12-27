package com.example.rntn.controller;

import com.example.rntn.dto.request.BatchPredictRequest;
import com.example.rntn.dto.request.PredictRequest;
import com.example.rntn.dto.response.AnalisisSentimientoResponse;
import com.example.rntn.dto.response.BatchPredictAggregateResponse;
import com.example.rntn.dto.response.BatchPredictResponse;
import com.example.rntn.dto.response.SentimentAggregateStats;
import com.example.rntn.model.SentimentLabel;
import com.example.rntn.service.SentimentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller REST para predicción de sentimientos usando modelo RNTN
 */
@RestController
@RequestMapping("/api/v1/sentiment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sentiment Analysis", description = "Endpoints para análisis de sentimientos con modelo RNTN")
public class SentimentController {

    private final SentimentService sentimentService;

    /**
     * Predice el sentimiento de un texto individual
     */
    @PostMapping("/predict")
    @PreAuthorize("hasPermission(null, 'sentiment:analyze')")
    @Operation(
        summary = "Predecir sentimiento de texto individual",
        description = "Analiza el sentimiento de un texto único usando el modelo RNTN. " +
                      "Retorna la clase predicha (0-4), label, nivel de confianza y riesgo."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Análisis completado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AnalisisSentimientoResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "500", description = "Error en el análisis")
    })
    public ResponseEntity<AnalisisSentimientoResponse> predict(
            @Valid @RequestBody PredictRequest request) {

        log.info("POST /api/v1/sentiment/predict - Texto: {}",
            request.getText().substring(0, Math.min(50, request.getText().length())));

        AnalisisSentimientoResponse response = sentimentService.analizarTexto(request.getText());

        return ResponseEntity.ok(response);
    }

    /**
     * Predice el sentimiento de múltiples textos (batch)
     */
    @PostMapping("/predict/batch")
    @PreAuthorize("hasPermission(null, 'sentiment:analyze_batch')")
    @Operation(
        summary = "Predecir sentimiento de múltiples textos",
        description = "Analiza el sentimiento de múltiples textos en una sola petición. " +
                      "Útil para procesar lotes de respuestas de evaluaciones."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lote procesado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BatchPredictResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<BatchPredictResponse> predictBatch(
            @Valid @RequestBody BatchPredictRequest request) {

        log.info("POST /api/v1/sentiment/predict/batch - Cantidad: {}", request.getTexts().size());

        List<AnalisisSentimientoResponse> results = request.getTexts().stream()
            .map(sentimentService::analizarTexto)
            .collect(Collectors.toList());

        BatchPredictResponse response = BatchPredictResponse.builder()
            .results(results)
            .processedCount(results.size())
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene la lista de labels de sentimiento soportados
     */
    @GetMapping("/labels")
    @PreAuthorize("hasPermission(null, 'sentiment:analyze')")
    @Operation(
        summary = "Obtener labels de sentimiento",
        description = "Retorna la lista de las 5 clases de sentimiento que el modelo puede predecir, " +
                      "con sus índices, nombres, descripciones y niveles de riesgo."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Labels obtenidos exitosamente"
        )
    })
    public ResponseEntity<Map<String, Object>> getLabels() {

        log.info("GET /api/v1/sentiment/labels");

        List<Map<String, Object>> labels = Arrays.stream(SentimentLabel.values())
            .map(label -> {
                Map<String, Object> labelInfo = new HashMap<>();
                labelInfo.put("id", label.getIndex());
                labelInfo.put("name", label.name());
                labelInfo.put("description", label.getDescription());
                labelInfo.put("riskLevel", label.getRiskLevel());
                return labelInfo;
            })
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("totalClasses", labels.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene estadísticas del modelo
     */
    @GetMapping("/model/stats")
    @PreAuthorize("hasPermission(null, 'sentiment:analyze')")
    @Operation(
        summary = "Obtener estadísticas del modelo",
        description = "Retorna información sobre el modelo RNTN cargado, incluyendo su ubicación, " +
                      "estado y clases soportadas."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Estadísticas obtenidas exitosamente"
        )
    })
    public ResponseEntity<Map<String, Object>> getModelStats() {

        log.info("GET /api/v1/sentiment/model/stats");

        Map<String, Object> stats = sentimentService.obtenerEstadisticasModelo();

        return ResponseEntity.ok(stats);
    }

    /**
     * ⭐ NUEVO: Predice el sentimiento de múltiples textos con análisis agregado
     * Combina predicción individual + estadísticas agregadas
     */
    @PostMapping("/predict/batch/aggregate")
    @PreAuthorize("hasPermission(null, 'sentiment:aggregate')")
    @Operation(
        summary = "Predecir sentimiento por lote con análisis agregado",
        description = "Analiza múltiples textos y retorna tanto los resultados individuales como " +
                      "estadísticas agregadas (distribución, confianza promedio, riesgo dominante, alertas). " +
                      "Ideal para análisis de sesiones completas de evaluación."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Análisis agregado completado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BatchPredictAggregateResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<BatchPredictAggregateResponse> predictBatchAggregate(
            @Valid @RequestBody BatchPredictRequest request) {

        log.info("POST /api/v1/sentiment/predict/batch/aggregate - Cantidad: {}", request.getTexts().size());

        // 1. Realizar análisis individual de cada texto
        List<AnalisisSentimientoResponse> individualResults = request.getTexts().stream()
            .map(sentimentService::analizarTexto)
            .collect(Collectors.toList());

        // 2. Calcular estadísticas agregadas
        SentimentAggregateStats aggregateStats = sentimentService.calcularEstadisticasAgregadas(individualResults);

        // 3. Construir respuesta completa
        BatchPredictAggregateResponse response = BatchPredictAggregateResponse.builder()
            .individualResults(individualResults)
            .aggregateAnalysis(aggregateStats)
            .processedCount(individualResults.size())
            .timestamp(LocalDateTime.now())
            .build();

        log.info("✅ Análisis agregado completado - Dominante: {}, Alertas: {}",
                 aggregateStats.getDominantSentiment(), aggregateStats.getHighRiskAlerts());

        return ResponseEntity.ok(response);
    }

    /**
     * ⭐ NUEVO: Obtiene estadísticas agregadas de respuestas guardadas en BD
     * Usa stored procedure para cálculo optimizado
     */
    @PostMapping("/aggregate/stats")
    @PreAuthorize("hasPermission(null, 'sentiment:aggregate')")
    @Operation(
        summary = "Calcular estadísticas agregadas desde BD",
        description = "Calcula estadísticas agregadas para un conjunto de respuestas ya almacenadas " +
                      "en la base de datos. Usa stored procedures para cálculo optimizado. " +
                      "Útil para generar reportes y dashboards."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Estadísticas calculadas exitosamente"
        ),
        @ApiResponse(responseCode = "400", description = "Lista de IDs inválida")
    })
    public ResponseEntity<SentimentAggregateStats> getAggregateStatsFromDB(
            @RequestBody List<Long> responseIds) {

        log.info("POST /api/v1/sentiment/aggregate/stats - IDs: {}", responseIds.size());

        SentimentAggregateStats stats = sentimentService.obtenerEstadisticasAgregadasDesdeBD(responseIds);

        return ResponseEntity.ok(stats);
    }

    /**
     * ⭐ NUEVO: Obtiene distribución de sentimientos por evaluación
     * Usa stored procedure para análisis de sesión completa
     */
    @GetMapping("/aggregate/evaluation/{idEvaluacion}")
    @PreAuthorize("hasPermission(null, 'sentiment:aggregate')")
    @Operation(
        summary = "Obtener distribución de sentimientos por evaluación",
        description = "Obtiene la distribución de sentimientos para todas las respuestas de una evaluación. " +
                      "Incluye información del paciente, profesional y estadísticas agregadas. " +
                      "Usa stored procedure para cálculo optimizado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Distribución obtenida exitosamente"
        ),
        @ApiResponse(responseCode = "404", description = "Evaluación no encontrada")
    })
    public ResponseEntity<Map<String, Object>> getDistributionByEvaluation(
            @Parameter(description = "ID de la evaluación", example = "1")
            @PathVariable Long idEvaluacion) {

        log.info("GET /api/v1/sentiment/aggregate/evaluation/{}", idEvaluacion);

        Map<String, Object> distribution = sentimentService.obtenerDistribucionPorEvaluacion(idEvaluacion);

        return ResponseEntity.ok(distribution);
    }

    /**
     * ⭐ NUEVO: Obtiene alertas de alto riesgo recientes
     * Detecta respuestas SUICIDAL con alta confianza
     */
    @GetMapping("/alerts/high-risk")
    @PreAuthorize("hasPermission(null, 'sentiment:aggregate')")
    @Operation(
        summary = "Obtener alertas de alto riesgo",
        description = "Obtiene todas las respuestas con indicadores de alto riesgo (SUICIDAL con confianza > 0.7) " +
                      "de los últimos N días. Incluye información completa del paciente, consulta y profesional. " +
                      "Crítico para seguimiento de pacientes en riesgo."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Alertas obtenidas exitosamente"
        )
    })
    public ResponseEntity<Map<String, Object>> getHighRiskAlerts(
            @Parameter(description = "Días hacia atrás", example = "7")
            @RequestParam(defaultValue = "7") Integer daysBack) {

        log.info("GET /api/v1/sentiment/alerts/high-risk?daysBack={}", daysBack);

        List<Map<String, Object>> alerts = sentimentService.obtenerAlertasAltoRiesgo(daysBack);

        Map<String, Object> response = new HashMap<>();
        response.put("alerts", alerts);
        response.put("totalAlerts", alerts.size());
        response.put("daysBack", daysBack);
        response.put("timestamp", LocalDateTime.now());

        log.warn("⚠️ Se encontraron {} alertas de alto riesgo en los últimos {} días", alerts.size(), daysBack);

        return ResponseEntity.ok(response);
    }
}

