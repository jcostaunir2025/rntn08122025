package com.example.rntn.service;

import com.example.rntn.dto.response.AnalisisSentimientoResponse;
import com.example.rntn.dto.response.SentimentAggregateStats;
import com.example.rntn.exception.PredictionException;
import com.example.rntn.model.SentimentLabel;
import com.example.rntn.repository.EvaluacionRespuestaRepository;
import com.example.rntn.util.SentimentPredictor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Servicio de análisis de sentimientos usando modelo RNTN
 * ⭐ SERVICIO CLAVE - Integra Stanford CoreNLP RNTN con la aplicación
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SentimentService {

    @Value("${rntn.model.default-path}")
    private String defaultModelPath;

    private final EvaluacionRespuestaRepository respuestaRepository;
    private SentimentPredictor predictor;

    /**
     * Inicializa el modelo RNTN al arrancar la aplicación
     * Se ejecuta una vez después de la construcción del bean
     */
    @PostConstruct
    public void init() {
        try {
            log.info("🚀 Inicializando SentimentService con modelo: {}", defaultModelPath);
            predictor = new SentimentPredictor(defaultModelPath);
            log.info("✅ Modelo RNTN cargado exitosamente desde: {}", defaultModelPath);
        } catch (Exception e) {
            log.error("❌ Error al cargar modelo RNTN desde: {}", defaultModelPath, e);
            throw new RuntimeException("No se pudo inicializar el servicio de análisis de sentimientos", e);
        }
    }

    /**
     * Analiza el sentimiento de un texto individual
     *
     * @param texto Texto a analizar
     * @return AnalisisSentimientoResponse con predicción y nivel de riesgo
     * @throws PredictionException si hay error en el análisis
     */
    public AnalisisSentimientoResponse analizarTexto(String texto) {
        try {
            if (texto == null || texto.trim().isEmpty()) {
                throw new PredictionException("El texto no puede estar vacío");
            }

            log.debug("Analizando texto: {}", texto.substring(0, Math.min(50, texto.length())));

            // Predecir con el modelo RNTN
            int predictedClass = predictor.predictClass(texto);

            // Mapear índice a label usando el enum
            SentimentLabel sentimentLabel = SentimentLabel.fromIndex(predictedClass);
            String predictedLabelName = sentimentLabel.name();

            // TODO: Implementar cálculo de confianza real del modelo
            // Por ahora, usamos un valor simulado basado en la longitud del texto
            double confidence = 0.75 + (Math.random() * 0.2); // Entre 0.75 y 0.95

            // Determinar nivel de riesgo según el label
            String nivelRiesgo = sentimentLabel.getRiskLevel();

            AnalisisSentimientoResponse response = AnalisisSentimientoResponse.builder()
                .texto(texto)
                .predictedClass(predictedClass)
                .predictedLabel(predictedLabelName)
                .confidence(confidence)
                .nivelRiesgo(nivelRiesgo)
                .timestamp(LocalDateTime.now())
                .build();

            log.info("✅ Análisis completado: {} (confidence: {}, riesgo: {})",
                     predictedLabelName, confidence, nivelRiesgo);

            // Log de alerta para riesgo alto
            if ("SUICIDAL".equals(predictedLabelName) && confidence > 0.7) {
                log.warn("⚠️ ALERTA RIESGO SUICIDA DETECTADO - Label: {}, Confidence: {}",
                         predictedLabelName, confidence);
            }

            return response;

        } catch (Exception e) {
            log.error("❌ Error al analizar texto", e);
            throw new PredictionException("Error en el análisis de sentimiento: " + e.getMessage(), e);
        }
    }

    /**
     * Analiza múltiples textos en lote de forma asíncrona
     *
     * @param textos Lista de textos a analizar
     * @return CompletableFuture con lista de análisis
     */
    public CompletableFuture<List<AnalisisSentimientoResponse>> analizarLote(List<String> textos) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("📊 Analizando lote de {} textos", textos.size());

            List<AnalisisSentimientoResponse> resultados = textos.stream()
                .map(this::analizarTexto)
                .collect(Collectors.toList());

            log.info("✅ Lote completado: {} análisis realizados", resultados.size());
            return resultados;
        });
    }

    /**
     * Obtiene estadísticas del modelo cargado
     */
    public java.util.Map<String, Object> obtenerEstadisticasModelo() {
        return java.util.Map.of(
            "modelPath", defaultModelPath,
            "status", "LOADED",
            "supportedLabels", java.util.Arrays.stream(SentimentLabel.values())
                .map(SentimentLabel::getName)
                .collect(Collectors.toList()),
            "totalClasses", SentimentLabel.values().length
        );
    }

    /**
     * Calcula estadísticas agregadas para una lista de resultados individuales
     * Usa lógica en memoria - ideal para batch predictions sin BD
     *
     * @param results Lista de análisis individuales
     * @return SentimentAggregateStats con estadísticas calculadas
     */
    public SentimentAggregateStats calcularEstadisticasAgregadas(List<AnalisisSentimientoResponse> results) {
        if (results == null || results.isEmpty()) {
            return SentimentAggregateStats.builder()
                .totalResponses(0)
                .sentimentDistribution(new HashMap<>())
                .build();
        }

        log.info("📊 Calculando estadísticas agregadas para {} resultados", results.size());

        // Distribución de sentimientos
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("ANXIETY", 0);
        distribution.put("SUICIDAL", 0);
        distribution.put("ANGER", 0);
        distribution.put("SADNESS", 0);
        distribution.put("FRUSTRATION", 0);

        int highRiskCount = 0;
        String highestRisk = "BAJO";
        double sumConfidence = 0.0;
        double minConf = Double.MAX_VALUE;
        double maxConf = Double.MIN_VALUE;

        // Procesar cada resultado
        for (AnalisisSentimientoResponse result : results) {
            String label = result.getPredictedLabel();

            // Actualizar distribución
            distribution.put(label, distribution.getOrDefault(label, 0) + 1);

            // Actualizar confianza
            double conf = result.getConfidence();
            sumConfidence += conf;
            minConf = Math.min(minConf, conf);
            maxConf = Math.max(maxConf, conf);

            // Detectar alertas de alto riesgo
            if ("SUICIDAL".equals(label) && conf > 0.7) {
                highRiskCount++;
            }

            // Determinar riesgo más alto
            String currentRisk = result.getNivelRiesgo();
            if ("ALTO".equals(currentRisk) ||
                ("MEDIO".equals(currentRisk) && !"ALTO".equals(highestRisk))) {
                highestRisk = currentRisk;
            }
        }

        // Calcular sentimiento dominante
        String dominantSentiment = distribution.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("UNKNOWN");

        double avgConfidence = sumConfidence / results.size();

        SentimentAggregateStats stats = SentimentAggregateStats.builder()
            .sentimentDistribution(distribution)
            .dominantSentiment(dominantSentiment)
            .averageConfidence(Math.round(avgConfidence * 1000.0) / 1000.0)
            .minConfidence(Math.round(minConf * 1000.0) / 1000.0)
            .maxConfidence(Math.round(maxConf * 1000.0) / 1000.0)
            .highestRiskLevel(highestRisk)
            .highRiskAlerts(highRiskCount)
            .totalResponses(results.size())
            .build();

        log.info("✅ Estadísticas calculadas - Dominante: {}, Avg Conf: {}, Alertas: {}",
                 dominantSentiment, avgConfidence, highRiskCount);

        return stats;
    }

    /**
     * Obtiene estadísticas agregadas usando stored procedure de BD
     * Usa IDs de respuestas ya guardadas en la base de datos
     *
     * @param responseIds Lista de IDs de EvaluacionRespuesta
     * @return SentimentAggregateStats calculadas por la BD
     */
    public SentimentAggregateStats obtenerEstadisticasAgregadasDesdeBD(List<Long> responseIds) {
        if (responseIds == null || responseIds.isEmpty()) {
            log.warn("⚠️ Lista de IDs vacía para estadísticas agregadas");
            return SentimentAggregateStats.builder()
                .totalResponses(0)
                .sentimentDistribution(new HashMap<>())
                .build();
        }

        log.info("📊 Obteniendo estadísticas agregadas desde BD para {} respuestas", responseIds.size());

        // Convertir lista de IDs a string separado por comas
        String idsStr = responseIds.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));

        try {
            // Llamar al stored procedure
            List<Map<String, Object>> resultList = respuestaRepository.getAggregateStats(idsStr);

            if (resultList.isEmpty()) {
                log.warn("⚠️ Stored procedure no retornó resultados");
                return SentimentAggregateStats.builder()
                    .totalResponses(0)
                    .sentimentDistribution(new HashMap<>())
                    .build();
            }

            // Procesar resultado del stored procedure
            Map<String, Object> result = resultList.get(0);

            // Construir distribución
            Map<String, Integer> distribution = new HashMap<>();
            distribution.put("ANXIETY", getIntValue(result, "count_anxiety"));
            distribution.put("SUICIDAL", getIntValue(result, "count_suicidal"));
            distribution.put("ANGER", getIntValue(result, "count_anger"));
            distribution.put("SADNESS", getIntValue(result, "count_sadness"));
            distribution.put("FRUSTRATION", getIntValue(result, "count_frustration"));

            SentimentAggregateStats stats = SentimentAggregateStats.builder()
                .sentimentDistribution(distribution)
                .dominantSentiment((String) result.get("dominant_sentiment"))
                .averageConfidence(getDoubleValue(result, "avg_confidence"))
                .minConfidence(getDoubleValue(result, "min_confidence"))
                .maxConfidence(getDoubleValue(result, "max_confidence"))
                .highestRiskLevel((String) result.get("highest_risk_level"))
                .highRiskAlerts(getIntValue(result, "high_risk_alerts"))
                .totalResponses(getIntValue(result, "total_respuestas"))
                .build();

            log.info("✅ Estadísticas obtenidas desde BD - Dominante: {}, Total: {}",
                     stats.getDominantSentiment(), stats.getTotalResponses());

            return stats;

        } catch (Exception e) {
            log.error("❌ Error al obtener estadísticas agregadas desde BD", e);
            throw new RuntimeException("Error al calcular estadísticas agregadas: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene distribución de sentimientos por evaluación
     */
    public Map<String, Object> obtenerDistribucionPorEvaluacion(Long idEvaluacion) {
        log.info("📊 Obteniendo distribución de sentimientos para evaluación {}", idEvaluacion);

        try {
            List<Map<String, Object>> resultList = respuestaRepository.getDistributionByEvaluation(idEvaluacion);

            if (resultList.isEmpty()) {
                log.warn("⚠️ No se encontró información para evaluación {}", idEvaluacion);
                return Map.of("error", "No se encontró la evaluación o no tiene respuestas");
            }

            Map<String, Object> result = resultList.get(0);
            log.info("✅ Distribución obtenida para evaluación {}", idEvaluacion);

            return result;

        } catch (Exception e) {
            log.error("❌ Error al obtener distribución por evaluación", e);
            throw new RuntimeException("Error al obtener distribución: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene alertas de alto riesgo de los últimos N días
     */
    public List<Map<String, Object>> obtenerAlertasAltoRiesgo(Integer daysBack) {
        log.info("⚠️ Obteniendo alertas de alto riesgo de los últimos {} días", daysBack);

        try {
            List<Map<String, Object>> alerts = respuestaRepository.getHighRiskAlerts(daysBack);
            log.info("✅ Se encontraron {} alertas de alto riesgo", alerts.size());

            return alerts;

        } catch (Exception e) {
            log.error("❌ Error al obtener alertas de alto riesgo", e);
            throw new RuntimeException("Error al obtener alertas: " + e.getMessage(), e);
        }
    }

    // ===== Métodos auxiliares =====

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof BigInteger) return ((BigInteger) value).intValue();
        if (value instanceof BigDecimal) return ((BigDecimal) value).intValue();
        return Integer.parseInt(value.toString());
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Float) return ((Float) value).doubleValue();
        if (value instanceof BigDecimal) return ((BigDecimal) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}

