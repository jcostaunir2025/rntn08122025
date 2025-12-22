package com.example.rntn.controller;

import com.example.rntn.dto.request.EvaluacionRequest;
import com.example.rntn.dto.request.EvaluacionRespuestaRequest;
import com.example.rntn.dto.response.EvaluacionResponse;
import com.example.rntn.dto.response.EvaluacionRespuestaResponse;
import com.example.rntn.service.EvaluacionCrudService;
import com.example.rntn.service.EvaluacionService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST para gestión de evaluaciones y análisis de sentimientos
 * ⭐ ENDPOINT PRINCIPAL - Integración RNTN + MySQL
 */
@RestController
@RequestMapping("/api/v1/evaluaciones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Evaluaciones", description = "API para gestión de evaluaciones psicológicas con análisis de sentimientos RNTN")
public class EvaluacionController {

    private final EvaluacionService evaluacionService;
    private final EvaluacionCrudService evaluacionCrudService;

    /**
     * ⭐ ENDPOINT PRINCIPAL - Registra una respuesta con análisis de sentimiento
     */
    @PostMapping("/respuestas")
    @Operation(
        summary = "Registrar respuesta con análisis de sentimiento",
        description = "Registra una respuesta de evaluación y automáticamente analiza el sentimiento usando el modelo RNTN. " +
                      "Si se detecta riesgo alto (SUICIDAL), se genera una alerta automática."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Respuesta registrada exitosamente con análisis de sentimiento",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EvaluacionRespuestaResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pregunta de evaluación no encontrada"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error en el análisis de sentimiento"
        )
    })
    public ResponseEntity<EvaluacionRespuestaResponse> registrarRespuesta(
            @Valid @RequestBody EvaluacionRespuestaRequest request) {

        log.info("POST /api/v1/evaluaciones/respuestas - Pregunta ID: {}, Analizar: {}",
            request.getIdEvaluacionPregunta(), request.isAnalizarSentimiento());

        EvaluacionRespuestaResponse response =
            evaluacionService.registrarRespuestaConAnalisis(request);

        // Log de alerta si es riesgo alto
        if (response.getSentimentAnalysis() != null &&
            "SUICIDAL".equals(response.getLabelEvaluacionRespuesta())) {
            log.warn("⚠️ ALERTA: Riesgo suicida detectado en respuesta ID: {}",
                response.getIdEvaluacionRespuesta());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Listar todas las respuestas con paginación
     */
    @GetMapping("/respuestas")
    @Operation(
        summary = "Listar todas las respuestas",
        description = "Retorna una lista paginada de todas las respuestas de evaluación con sus análisis"
    )
    public ResponseEntity<org.springframework.data.domain.Page<EvaluacionRespuestaResponse>> listarRespuestas(
            @org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable) {

        log.info("GET /api/v1/evaluaciones/respuestas - Page: {}", pageable.getPageNumber());
        org.springframework.data.domain.Page<EvaluacionRespuestaResponse> respuestas =
            evaluacionService.listarTodasLasRespuestas(pageable);
        return ResponseEntity.ok(respuestas);
    }

    /**
     * Obtener una respuesta específica por ID
     */
    @GetMapping("/respuestas/{id}")
    @Operation(
        summary = "Obtener respuesta por ID",
        description = "Retorna una respuesta de evaluación específica con su análisis de sentimiento"
    )
    public ResponseEntity<EvaluacionRespuestaResponse> obtenerRespuesta(@PathVariable Integer id) {
        log.info("GET /api/v1/evaluaciones/respuestas/{}", id);
        EvaluacionRespuestaResponse response = evaluacionService.obtenerRespuesta(id);
        return ResponseEntity.ok(response);
    }


    /**
     * Buscar respuestas por label de sentimiento
     */
    @GetMapping("/respuestas/label/{label}")
    @Operation(
        summary = "Buscar respuestas por label",
        description = "Filtra respuestas por label de sentimiento (ANXIETY, SUICIDAL, ANGER, SADNESS, FRUSTRATION)"
    )
    public ResponseEntity<List<EvaluacionRespuestaResponse>> buscarRespuestasPorLabel(@PathVariable String label) {
        log.info("GET /api/v1/evaluaciones/respuestas/label/{}", label);
        List<EvaluacionRespuestaResponse> respuestas =
            evaluacionService.buscarRespuestasPorLabel(label);
        return ResponseEntity.ok(respuestas);
    }

    /**
     * Obtener respuestas de alto riesgo
     */
    @GetMapping("/respuestas/alto-riesgo")
    @Operation(
        summary = "Obtener respuestas de alto riesgo",
        description = "Retorna respuestas con label SUICIDAL y alta confianza"
    )
    public ResponseEntity<List<EvaluacionRespuestaResponse>> obtenerRespuestasAltoRiesgo(
            @RequestParam(defaultValue = "0.7") Double umbral) {

        log.info("GET /api/v1/evaluaciones/respuestas/alto-riesgo - Umbral: {}", umbral);
        List<EvaluacionRespuestaResponse> respuestas =
            evaluacionService.obtenerRespuestasAltoRiesgo(umbral);
        return ResponseEntity.ok(respuestas);
    }

    /**
     * Actualizar una respuesta existente
     */
    @PutMapping("/respuestas/{id}")
    @Operation(
        summary = "Actualizar respuesta",
        description = "Actualiza el texto de una respuesta y recalcula el análisis de sentimiento"
    )
    public ResponseEntity<EvaluacionRespuestaResponse> actualizarRespuesta(
            @PathVariable Integer id,
            @Valid @RequestBody EvaluacionRespuestaRequest request) {

        log.info("PUT /api/v1/evaluaciones/respuestas/{}", id);
        EvaluacionRespuestaResponse response =
            evaluacionService.actualizarRespuesta(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar una respuesta
     */
    @DeleteMapping("/respuestas/{id}")
    @Operation(summary = "Eliminar respuesta")
    public ResponseEntity<Void> eliminarRespuesta(@PathVariable Integer id) {
        log.info("DELETE /api/v1/evaluaciones/respuestas/{}", id);
        evaluacionService.eliminarRespuesta(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene análisis agregado de múltiples preguntas
     */
    @GetMapping("/analisis-agregado")
    @Operation(
        summary = "Obtener análisis agregado",
        description = "Retorna estadísticas agregadas de sentimientos para un conjunto de preguntas. " +
                      "Incluye distribución, sentimiento dominante, nivel de riesgo y alertas."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Análisis agregado obtenido exitosamente"
        )
    })
    public ResponseEntity<Map<String, Object>> obtenerAnalisisAgregado(
            @Parameter(description = "Lista de IDs de preguntas a analizar", required = true)
            @RequestParam List<Integer> preguntaIds) {

        log.info("GET /api/v1/evaluaciones/analisis-agregado - Preguntas: {}", preguntaIds);

        Map<String, Object> analisis = evaluacionService.obtenerAnalisisAgregado(preguntaIds);

        return ResponseEntity.ok(analisis);
    }

    /**
     * CRUD para Evaluacion
     */
    @PostMapping
    @Operation(
        summary = "Crear evaluación",
        description = "Registra una nueva evaluación psicológica"
    )
    public ResponseEntity<EvaluacionResponse> crearEvaluacion(
            @Valid @RequestBody EvaluacionRequest request) {

        log.info("POST /api/v1/evaluaciones - Crear evaluación: {}", request);

        EvaluacionResponse response = evaluacionCrudService.crearEvaluacion(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener una evaluación por ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener evaluación por ID",
        description = "Retorna los detalles de una evaluación psicológica por su ID"
    )
    public ResponseEntity<EvaluacionResponse> obtenerEvaluacion(@PathVariable Integer id) {
        log.info("GET /api/v1/evaluaciones/{}", id);
        EvaluacionResponse response = evaluacionCrudService.obtenerEvaluacion(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar una evaluación existente
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar evaluación",
        description = "Actualiza los detalles de una evaluación psicológica"
    )
    public ResponseEntity<EvaluacionResponse> actualizarEvaluacion(
            @PathVariable Integer id,
            @Valid @RequestBody EvaluacionRequest request) {

        log.info("PUT /api/v1/evaluaciones/{}", id);
        EvaluacionResponse response = evaluacionCrudService.actualizarEvaluacion(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar una evaluación
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar evaluación")
    public ResponseEntity<Void> eliminarEvaluacion(@PathVariable Integer id) {
        log.info("DELETE /api/v1/evaluaciones/{}", id);
        evaluacionCrudService.eliminarEvaluacion(id);
        return ResponseEntity.noContent().build();
    }
}
