package com.example.rntn.controller;

import com.example.rntn.dto.request.EvaluacionRequest;
import com.example.rntn.dto.response.EvaluacionResponse;
import com.example.rntn.service.EvaluacionCrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para gestión de evaluaciones psicológicas
 */
@RestController
@RequestMapping("/api/v1/evaluaciones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Evaluaciones", description = "API para gestión de evaluaciones psicológicas")
public class EvaluacionController {

    private final EvaluacionCrudService evaluacionCrudService;


    /**
     * CRUD para Evaluacion
     */
    @PostMapping
    @PreAuthorize("hasPermission(null, 'evaluacion:create')")
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
    @PreAuthorize("hasPermission(null, 'evaluacion:read')")
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
    @PreAuthorize("hasPermission(null, 'evaluacion:update')")
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
    @PreAuthorize("hasPermission(null, 'evaluacion:delete')")
    @Operation(summary = "Eliminar evaluación")
    public ResponseEntity<Void> eliminarEvaluacion(@PathVariable Integer id) {
        log.info("DELETE /api/v1/evaluaciones/{}", id);
        evaluacionCrudService.eliminarEvaluacion(id);
        return ResponseEntity.noContent().build();
    }
}
