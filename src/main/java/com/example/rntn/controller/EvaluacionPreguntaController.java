package com.example.rntn.controller;

import com.example.rntn.dto.request.EvaluacionPreguntaRequest;
import com.example.rntn.dto.response.EvaluacionPreguntaResponse;
import com.example.rntn.service.EvaluacionPreguntaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestión de Preguntas de Evaluación
 */
@RestController
@RequestMapping("/api/v1/preguntas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Preguntas de Evaluación", description = "API para gestión de preguntas de evaluación")
public class EvaluacionPreguntaController {

    private final EvaluacionPreguntaService preguntaService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'evaluacion_pregunta:create')")
    @Operation(summary = "Crear nueva pregunta", description = "Crea una nueva pregunta de evaluación")
    public ResponseEntity<EvaluacionPreguntaResponse> crearPregunta(
            @Valid @RequestBody EvaluacionPreguntaRequest request) {

        log.info("POST /api/v1/preguntas");
        EvaluacionPreguntaResponse response = preguntaService.crearPregunta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'evaluacion_pregunta:read')")
    @Operation(summary = "Obtener pregunta por ID")
    public ResponseEntity<EvaluacionPreguntaResponse> obtenerPregunta(@PathVariable Integer id) {
        log.info("GET /api/v1/preguntas/{}", id);
        EvaluacionPreguntaResponse response = preguntaService.obtenerPregunta(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'evaluacion_pregunta:read')")
    @Operation(summary = "Listar preguntas", description = "Lista todas las preguntas de evaluación")
    public ResponseEntity<Page<EvaluacionPreguntaResponse>> listarPreguntas(
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/preguntas - Page: {}", pageable.getPageNumber());
        Page<EvaluacionPreguntaResponse> response = preguntaService.listarPreguntas(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'evaluacion_pregunta:update')")
    @Operation(summary = "Actualizar pregunta")
    public ResponseEntity<EvaluacionPreguntaResponse> actualizarPregunta(
            @PathVariable Integer id,
            @Valid @RequestBody EvaluacionPreguntaRequest request) {

        log.info("PUT /api/v1/preguntas/{}", id);
        EvaluacionPreguntaResponse response = preguntaService.actualizarPregunta(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'evaluacion_pregunta:delete')")
    @Operation(summary = "Eliminar pregunta")
    public ResponseEntity<Void> eliminarPregunta(@PathVariable Integer id) {
        log.info("DELETE /api/v1/preguntas/{}", id);
        preguntaService.eliminarPregunta(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Listar respuestas de una pregunta específica
     */
    @GetMapping("/{idPregunta}/respuestas")
    @Operation(
        summary = "Listar respuestas por pregunta",
        description = "Retorna todas las respuestas asociadas a una pregunta de evaluación"
    )
    public ResponseEntity<List<com.example.rntn.dto.response.EvaluacionRespuestaResponse>> listarRespuestasPorPregunta(
            @PathVariable Integer idPregunta) {

        log.info("GET /api/v1/preguntas/{}/respuestas", idPregunta);
        List<com.example.rntn.dto.response.EvaluacionRespuestaResponse> respuestas =
            preguntaService.listarRespuestasPorPregunta(idPregunta);
        return ResponseEntity.ok(respuestas);
    }
}
