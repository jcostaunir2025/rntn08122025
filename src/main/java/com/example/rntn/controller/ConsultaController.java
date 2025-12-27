package com.example.rntn.controller;

import com.example.rntn.dto.request.ConsultaRequest;
import com.example.rntn.dto.response.ConsultaResponse;
import com.example.rntn.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/consultas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Consultas", description = "API para gestión de consultas médicas")
public class ConsultaController {

    private final ConsultaService consultaService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'consulta:create')")
    @Operation(summary = "Crear nueva consulta", description = "Registra una nueva consulta médica")
    public ResponseEntity<ConsultaResponse> crearConsulta(@Valid @RequestBody ConsultaRequest request) {
        log.info("POST /api/v1/consultas - Paciente: {}, Personal: {}",
            request.getIdPaciente(), request.getIdPersonal());

        ConsultaResponse response = consultaService.crearConsulta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'consulta:read')")
    @Operation(summary = "Obtener consulta por ID")
    public ResponseEntity<ConsultaResponse> obtenerConsulta(@PathVariable Integer id) {
        log.info("GET /api/v1/consultas/{}", id);
        ConsultaResponse response = consultaService.obtenerConsulta(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paciente/{idPaciente}")
    @PreAuthorize("hasPermission(null, 'consulta:read')")
    @Operation(summary = "Obtener consultas por paciente",
               description = "Lista todas las consultas de un paciente con filtros de fecha opcionales")
    public ResponseEntity<Page<ConsultaResponse>> listarConsultasPorPaciente(
            @Parameter(description = "ID del paciente", required = true)
            @PathVariable Integer idPaciente,
            @Parameter(description = "Fecha desde (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @Parameter(description = "Fecha hasta (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/consultas/paciente/{} - Desde: {}, Hasta: {}", idPaciente, desde, hasta);

        Page<ConsultaResponse> response = consultaService.listarConsultasPorPaciente(
            idPaciente, desde, hasta, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/personal/{idPersonal}")
    @PreAuthorize("hasPermission(null, 'consulta:read')")
    @Operation(summary = "Obtener consultas por personal médico")
    public ResponseEntity<Page<ConsultaResponse>> listarConsultasPorPersonal(
            @PathVariable Integer idPersonal,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/consultas/personal/{}", idPersonal);

        Page<ConsultaResponse> response = consultaService.listarConsultasPorPersonal(idPersonal, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasPermission(null, 'consulta:update')")
    @Operation(summary = "Actualizar estado de consulta",
               description = "Actualiza el estado de la consulta. IDs válidos: 1=PENDIENTE, 2=EN_PROGRESO, 3=COMPLETADA, 4=CANCELADA, 5=REPROGRAMADA, 6=NO_ASISTIO")
    public ResponseEntity<ConsultaResponse> actualizarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> request) {

        Integer nuevoEstatusId = request.get("estatusConsulta");
        log.info("PATCH /api/v1/consultas/{}/estado - Nuevo estado ID: {}", id, nuevoEstatusId);

        ConsultaResponse response = consultaService.actualizarEstadoConsulta(id, nuevoEstatusId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasPermission(null, 'consulta:update')")
    @Operation(summary = "Finalizar consulta", description = "Marca la consulta como completada")
    public ResponseEntity<ConsultaResponse> finalizarConsulta(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, String> request) {

        log.info("POST /api/v1/consultas/{}/finalizar", id);

        LocalDateTime fechaFin = null;
        if (request != null && request.containsKey("fechafinConsulta")) {
            fechaFin = LocalDateTime.parse(request.get("fechafinConsulta"));
        }

        ConsultaResponse response = consultaService.finalizarConsulta(id, fechaFin);
        return ResponseEntity.ok(response);
    }
}

