package com.example.rntn.service;

import com.example.rntn.dto.request.EvaluacionRespuestaRequest;
import com.example.rntn.dto.response.AnalisisSentimientoResponse;
import com.example.rntn.dto.response.EvaluacionRespuestaResponse;
import com.example.rntn.entity.EvaluacionPregunta;
import com.example.rntn.entity.EvaluacionRespuesta;
import com.example.rntn.exception.ResourceNotFoundException;
import com.example.rntn.repository.EvaluacionPreguntaRepository;
import com.example.rntn.repository.EvaluacionRespuestaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para Evaluaciones
 * ⭐ INTEGRA análisis de sentimientos RNTN con persistencia en BD
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvaluacionService {

    private final EvaluacionPreguntaRepository preguntaRepository;
    private final EvaluacionRespuestaRepository respuestaRepository;
    private final SentimentService sentimentService;

    /**
     * Registra una respuesta con análisis automático de sentimiento
     * ⭐ MÉTODO CLAVE - Integración RNTN + MySQL
     *
     * @param request Request con texto de respuesta
     * @return Response con análisis de sentimiento incluido
     */
    public EvaluacionRespuestaResponse registrarRespuestaConAnalisis(
            EvaluacionRespuestaRequest request) {

        // Validar que existe la pregunta
        EvaluacionPregunta pregunta = preguntaRepository.findById(request.getIdEvaluacionPregunta())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Pregunta no encontrada: " + request.getIdEvaluacionPregunta()));

        // Analizar sentimiento si está habilitado
        AnalisisSentimientoResponse analisis = null;
        String label = null;
        Double confidence = null;

        if (request.isAnalizarSentimiento()) {
            log.info("🔍 Analizando sentimiento para respuesta de pregunta ID: {}",
                request.getIdEvaluacionPregunta());

            analisis = sentimentService.analizarTexto(request.getTextoEvaluacionRespuesta());
            label = analisis.getPredictedLabel();
            confidence = analisis.getConfidence();

            // Registrar alerta si es riesgo alto
            if ("SUICIDAL".equals(label) && confidence > 0.7) {
                log.warn("⚠️ ALERTA RIESGO SUICIDA - Pregunta ID: {}, Confidence: {:.2f}",
                    request.getIdEvaluacionPregunta(), confidence);
                // TODO: Enviar notificación urgente (email, SMS, webhook)
            }
        }

        // Crear entidad de respuesta
        EvaluacionRespuesta respuesta = EvaluacionRespuesta.builder()
            .evaluacionPregunta(pregunta)
            .textoEvaluacionRespuesta(request.getTextoEvaluacionRespuesta())
            .textoSetEvaluacionRespuesta(
                request.getTextoEvaluacionRespuesta().toLowerCase().trim())
            .labelEvaluacionRespuesta(label)
            .confidenceScore(confidence)
            .build();

        // Guardar en base de datos
        respuesta = respuestaRepository.save(respuesta);

        log.info("✅ Respuesta guardada: ID={}, Label={}",
            respuesta.getIdEvaluacionRespuesta(), label);

        // Mapear a DTO de respuesta
        EvaluacionRespuestaResponse response = EvaluacionRespuestaResponse.builder()
            .idEvaluacionRespuesta(respuesta.getIdEvaluacionRespuesta())
            .idEvaluacionPregunta(pregunta.getIdEvaluacionPregunta())
            .textoPregunta(pregunta.getTextoEvaluacionPregunta())
            .textoEvaluacionRespuesta(respuesta.getTextoEvaluacionRespuesta())
            .textoSetEvaluacionRespuesta(respuesta.getTextoSetEvaluacionRespuesta())
            .labelEvaluacionRespuesta(respuesta.getLabelEvaluacionRespuesta())
            .confidenceScore(respuesta.getConfidenceScore())
            .sentimentAnalysis(analisis)
            .createdAt(respuesta.getCreatedAt())
            .build();

        return response;
    }

    /**
     * Obtiene el análisis agregado de todas las respuestas
     *
     * @param preguntaIds Lista de IDs de preguntas a analizar
     * @return Mapa con estadísticas y distribución de sentimientos
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerAnalisisAgregado(List<Integer> preguntaIds) {

        // Obtener todas las respuestas
        List<EvaluacionRespuesta> respuestas = preguntaIds.stream()
            .flatMap(id -> respuestaRepository
                .findByEvaluacionPreguntaIdEvaluacionPregunta(id).stream())
            .collect(Collectors.toList());

        // Calcular distribución de sentimientos
        Map<String, Long> distribucion = respuestas.stream()
            .filter(r -> r.getLabelEvaluacionRespuesta() != null)
            .collect(Collectors.groupingBy(
                EvaluacionRespuesta::getLabelEvaluacionRespuesta,
                Collectors.counting()
            ));

        // Determinar sentimiento dominante
        String sentimientoDominante = distribucion.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("UNKNOWN");

        // Calcular nivel de riesgo
        String nivelRiesgo = calcularNivelRiesgo(distribucion, respuestas.size());

        // Detectar alertas
        List<Map<String, Object>> alertas = respuestas.stream()
            .filter(r -> "SUICIDAL".equals(r.getLabelEvaluacionRespuesta())
                && r.getConfidenceScore() != null
                && r.getConfidenceScore() > 0.7)
            .map(r -> {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("tipo", "RIESGO_SUICIDA");
                alerta.put("nivel", "ALTO");
                alerta.put("respuesta", r.getTextoEvaluacionRespuesta());
                alerta.put("confidence", r.getConfidenceScore());
                return alerta;
            })
            .collect(Collectors.toList());

        // Construir respuesta
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalRespuestas", respuestas.size());
        resultado.put("distribucionSentimientos", distribucion);
        resultado.put("sentimientoDominante", sentimientoDominante);
        resultado.put("nivelRiesgo", nivelRiesgo);
        resultado.put("alertas", alertas);

        return resultado;
    }

    /**
     * Calcula el nivel de riesgo basado en la distribución de sentimientos
     */
    private String calcularNivelRiesgo(Map<String, Long> distribucion, int total) {
        long suicidal = distribucion.getOrDefault("SUICIDAL", 0L);
        long anxiety = distribucion.getOrDefault("ANXIETY", 0L);
        long anger = distribucion.getOrDefault("ANGER", 0L);
        long sadness = distribucion.getOrDefault("SADNESS", 0L);

        // Si hay pensamiento suicida, es riesgo ALTO
        if (suicidal > 0) {
            return "ALTO";
        }

        // Calcular porcentaje de sentimientos negativos de riesgo medio
        double riesgoMedio = (anxiety + anger + sadness) / (double) total;
        if (riesgoMedio > 0.5) {
            return "MEDIO";
        }

        return "BAJO";
    }

    /**
     * Listar todas las respuestas con paginación
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<EvaluacionRespuestaResponse> listarTodasLasRespuestas(
            org.springframework.data.domain.Pageable pageable) {

        org.springframework.data.domain.Page<EvaluacionRespuesta> respuestas =
            respuestaRepository.findAll(pageable);

        return respuestas.map(this::mapRespuestaToResponse);
    }

    /**
     * Obtener una respuesta por ID
     */
    @Transactional(readOnly = true)
    public EvaluacionRespuestaResponse obtenerRespuesta(Integer id) {
        EvaluacionRespuesta respuesta = respuestaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Respuesta no encontrada: " + id));

        return mapRespuestaToResponse(respuesta);
    }

    /**
     * Listar respuestas por pregunta
     */
    @Transactional(readOnly = true)
    public List<EvaluacionRespuestaResponse> listarRespuestasPorPregunta(Integer idPregunta) {
        List<EvaluacionRespuesta> respuestas =
            respuestaRepository.findByEvaluacionPreguntaIdEvaluacionPregunta(idPregunta);

        return respuestas.stream()
            .map(this::mapRespuestaToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Buscar respuestas por label
     */
    @Transactional(readOnly = true)
    public List<EvaluacionRespuestaResponse> buscarRespuestasPorLabel(String label) {
        List<EvaluacionRespuesta> respuestas = respuestaRepository.findByLabel(label);

        return respuestas.stream()
            .map(this::mapRespuestaToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtener respuestas de alto riesgo
     */
    @Transactional(readOnly = true)
    public List<EvaluacionRespuestaResponse> obtenerRespuestasAltoRiesgo(Double umbral) {
        List<EvaluacionRespuesta> respuestas =
            respuestaRepository.findHighRiskResponses(umbral);

        return respuestas.stream()
            .map(this::mapRespuestaToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Actualizar respuesta con re-análisis
     */
    public EvaluacionRespuestaResponse actualizarRespuesta(Integer id, EvaluacionRespuestaRequest request) {
        log.info("Actualizando respuesta: {}", id);

        EvaluacionRespuesta respuesta = respuestaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Respuesta no encontrada: " + id));

        // Actualizar texto
        respuesta.setTextoEvaluacionRespuesta(request.getTextoEvaluacionRespuesta());
        respuesta.setTextoSetEvaluacionRespuesta(
            request.getTextoEvaluacionRespuesta().toLowerCase().trim());

        // Re-analizar si está habilitado
        AnalisisSentimientoResponse analisis = null;
        if (request.isAnalizarSentimiento()) {
            analisis = sentimentService.analizarTexto(request.getTextoEvaluacionRespuesta());
            respuesta.setLabelEvaluacionRespuesta(analisis.getPredictedLabel());
            respuesta.setConfidenceScore(analisis.getConfidence());
        }

        respuesta = respuestaRepository.save(respuesta);

        return mapRespuestaToResponse(respuesta, analisis);
    }

    /**
     * Eliminar respuesta
     */
    public void eliminarRespuesta(Integer id) {
        log.info("Eliminando respuesta: {}", id);

        EvaluacionRespuesta respuesta = respuestaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Respuesta no encontrada: " + id));

        respuestaRepository.delete(respuesta);
    }

    /**
     * Mapear respuesta a DTO
     */
    private EvaluacionRespuestaResponse mapRespuestaToResponse(EvaluacionRespuesta respuesta) {
        return mapRespuestaToResponse(respuesta, null);
    }

    private EvaluacionRespuestaResponse mapRespuestaToResponse(
            EvaluacionRespuesta respuesta, AnalisisSentimientoResponse analisis) {

        return EvaluacionRespuestaResponse.builder()
            .idEvaluacionRespuesta(respuesta.getIdEvaluacionRespuesta())
            .idEvaluacionPregunta(respuesta.getEvaluacionPregunta().getIdEvaluacionPregunta())
            .textoPregunta(respuesta.getEvaluacionPregunta().getTextoEvaluacionPregunta())
            .textoEvaluacionRespuesta(respuesta.getTextoEvaluacionRespuesta())
            .textoSetEvaluacionRespuesta(respuesta.getTextoSetEvaluacionRespuesta())
            .labelEvaluacionRespuesta(respuesta.getLabelEvaluacionRespuesta())
            .confidenceScore(respuesta.getConfidenceScore())
            .sentimentAnalysis(analisis)
            .createdAt(respuesta.getCreatedAt())
            .build();
    }
}
