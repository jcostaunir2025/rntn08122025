package com.example.rntn.controller;

import com.example.rntn.dto.request.ReporteRequest;
import com.example.rntn.dto.response.ReporteResponse;
import com.example.rntn.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para gestión de Reportes
 */
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reportes", description = "API para gestión de reportes de evaluaciones")
public class ReporteController {

    private final ReporteService reporteService;

    @PostMapping
    @Operation(summary = "Generar nuevo reporte",
               description = "Genera un nuevo reporte basado en una evaluación")
    public ResponseEntity<ReporteResponse> generarReporte(@Valid @RequestBody ReporteRequest request) {
        log.info("POST /api/v1/reportes - Nombre: {}", request.getNombreReporte());
        ReporteResponse response = reporteService.generarReporte(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reporte por ID")
    public ResponseEntity<ReporteResponse> obtenerReporte(@PathVariable Integer id) {
        log.info("GET /api/v1/reportes/{}", id);
        ReporteResponse response = reporteService.obtenerReporte(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos los reportes")
    public ResponseEntity<Page<ReporteResponse>> listarReportes(
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/reportes - Page: {}", pageable.getPageNumber());
        Page<ReporteResponse> response = reporteService.listarReportes(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Listar reportes por usuario",
               description = "Lista todos los reportes generados por un usuario específico")
    public ResponseEntity<Page<ReporteResponse>> listarReportesPorUsuario(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Integer idUsuario,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/reportes/usuario/{}", idUsuario);
        Page<ReporteResponse> response = reporteService.listarReportesPorUsuario(idUsuario, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/evaluacion/{idEvaluacion}")
    @Operation(summary = "Listar reportes por evaluación",
               description = "Lista todos los reportes asociados a una evaluación específica")
    public ResponseEntity<Page<ReporteResponse>> listarReportesPorEvaluacion(
            @Parameter(description = "ID de la evaluación", required = true)
            @PathVariable Integer idEvaluacion,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/reportes/evaluacion/{}", idEvaluacion);
        Page<ReporteResponse> response = reporteService.listarReportesPorEvaluacion(idEvaluacion, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar reporte")
    public ResponseEntity<ReporteResponse> actualizarReporte(
            @PathVariable Integer id,
            @Valid @RequestBody ReporteRequest request) {

        log.info("PUT /api/v1/reportes/{}", id);
        ReporteResponse response = reporteService.actualizarReporte(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar reporte")
    public ResponseEntity<Void> eliminarReporte(@PathVariable Integer id) {
        log.info("DELETE /api/v1/reportes/{}", id);
        reporteService.eliminarReporte(id);
        return ResponseEntity.noContent().build();
    }
}

