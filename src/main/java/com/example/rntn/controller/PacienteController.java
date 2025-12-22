package com.example.rntn.controller;

import com.example.rntn.dto.request.PacienteRequest;
import com.example.rntn.dto.response.PacienteResponse;
import com.example.rntn.service.PacienteService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para gestión de Pacientes
 */
@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pacientes", description = "API para gestión de pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    @PostMapping
    @Operation(
        summary = "Crear nuevo paciente",
        description = "Registra un nuevo paciente en el sistema"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Paciente creado exitosamente",
            content = @Content(schema = @Schema(implementation = PacienteResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o documento duplicado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<PacienteResponse> crearPaciente(
            @Valid @RequestBody PacienteRequest request) {

        log.info("POST /api/v1/pacientes - Doc: {}", request.getDocPaciente());

        PacienteResponse response = pacienteService.crearPaciente(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener paciente por ID",
        description = "Retorna la información de un paciente específico"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Paciente encontrado",
            content = @Content(schema = @Schema(implementation = PacienteResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    public ResponseEntity<PacienteResponse> obtenerPaciente(
            @Parameter(description = "ID del paciente", required = true)
            @PathVariable Integer id) {

        log.info("GET /api/v1/pacientes/{}", id);

        PacienteResponse response = pacienteService.obtenerPaciente(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
        summary = "Listar pacientes",
        description = "Retorna una lista paginada de pacientes con filtros opcionales"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de pacientes obtenida exitosamente"
        )
    })
    public ResponseEntity<Page<PacienteResponse>> listarPacientes(
            @Parameter(description = "Filtrar por estatus")
            @RequestParam(required = false) String estatus,
            @Parameter(description = "Buscar por nombre o documento")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/pacientes - Estatus: {}, Search: {}, Page: {}",
            estatus, search, pageable.getPageNumber());

        Page<PacienteResponse> response = pacienteService.listarPacientes(estatus, search, pageable);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar paciente",
        description = "Actualiza la información de un paciente existente"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Paciente actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = PacienteResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Paciente no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<PacienteResponse> actualizarPaciente(
            @Parameter(description = "ID del paciente", required = true)
            @PathVariable Integer id,
            @Valid @RequestBody PacienteRequest request) {

        log.info("PUT /api/v1/pacientes/{}", id);

        PacienteResponse response = pacienteService.actualizarPaciente(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar paciente",
        description = "Desactiva un paciente (soft delete)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Paciente eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    public ResponseEntity<Void> eliminarPaciente(
            @Parameter(description = "ID del paciente", required = true)
            @PathVariable Integer id) {

        log.info("DELETE /api/v1/pacientes/{}", id);

        pacienteService.eliminarPaciente(id);

        return ResponseEntity.noContent().build();
    }
}

