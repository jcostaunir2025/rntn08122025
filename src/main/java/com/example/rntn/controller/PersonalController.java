package com.example.rntn.controller;

import com.example.rntn.dto.request.PersonalRequest;
import com.example.rntn.dto.response.PersonalResponse;
import com.example.rntn.service.PersonalService;
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

@RestController
@RequestMapping("/api/v1/personal")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Personal Médico", description = "API para gestión de personal médico")
public class PersonalController {

    private final PersonalService personalService;

    @PostMapping
    @Operation(summary = "Crear nuevo personal médico")
    public ResponseEntity<PersonalResponse> crearPersonal(@Valid @RequestBody PersonalRequest request) {
        log.info("POST /api/v1/personal - Doc: {}", request.getDocPersonal());
        PersonalResponse response = personalService.crearPersonal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener personal por ID")
    public ResponseEntity<PersonalResponse> obtenerPersonal(@PathVariable Integer id) {
        log.info("GET /api/v1/personal/{}", id);
        PersonalResponse response = personalService.obtenerPersonal(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar personal médico")
    public ResponseEntity<Page<PersonalResponse>> listarPersonal(
            @Parameter(description = "Filtrar por estatus")
            @RequestParam(required = false) String estatus,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/personal - Estatus: {}", estatus);
        Page<PersonalResponse> response = personalService.listarPersonal(estatus, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar personal")
    public ResponseEntity<PersonalResponse> actualizarPersonal(
            @PathVariable Integer id,
            @Valid @RequestBody PersonalRequest request) {

        log.info("PUT /api/v1/personal/{}", id);
        PersonalResponse response = personalService.actualizarPersonal(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar personal")
    public ResponseEntity<Void> eliminarPersonal(@PathVariable Integer id) {
        log.info("DELETE /api/v1/personal/{}", id);
        personalService.eliminarPersonal(id);
        return ResponseEntity.noContent().build();
    }
}

