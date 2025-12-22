package com.example.rntn.controller;

import com.example.rntn.dto.request.UsuarioRequest;
import com.example.rntn.dto.response.UsuarioResponse;
import com.example.rntn.service.UsuarioService;
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

import java.util.List;
import java.util.Map;

/**
 * Controller REST para gestión de Usuarios
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "API para gestión de usuarios del sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Crear nuevo usuario", description = "Registra un nuevo usuario con roles asignados")
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        log.info("POST /api/v1/usuarios - Usuario: {}", request.getNombreUsuario());
        UsuarioResponse response = usuarioService.crearUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UsuarioResponse> obtenerUsuario(@PathVariable Integer id) {
        log.info("GET /api/v1/usuarios/{}", id);
        UsuarioResponse response = usuarioService.obtenerUsuario(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nombre/{nombreUsuario}")
    @Operation(summary = "Obtener usuario por nombre")
    public ResponseEntity<UsuarioResponse> obtenerPorNombre(
            @Parameter(description = "Nombre de usuario", required = true)
            @PathVariable String nombreUsuario) {

        log.info("GET /api/v1/usuarios/nombre/{}", nombreUsuario);
        UsuarioResponse response = usuarioService.obtenerPorNombre(nombreUsuario);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar usuarios")
    public ResponseEntity<Page<UsuarioResponse>> listarUsuarios(
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/usuarios - Page: {}", pageable.getPageNumber());
        Page<UsuarioResponse> response = usuarioService.listarUsuarios(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioRequest request) {

        log.info("PUT /api/v1/usuarios/{}", id);
        UsuarioResponse response = usuarioService.actualizarUsuario(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
        log.info("DELETE /api/v1/usuarios/{}", id);
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Listar todos los roles disponibles
     */
    @GetMapping("/roles")
    @Operation(summary = "Listar roles disponibles",
            description = "Retorna todos los roles del sistema (ADMIN, DOCTOR, ENFERMERO, etc.)")
    public ResponseEntity<List<Map<String, Object>>> listarRoles() {
        log.info("GET /api/v1/usuarios/roles");
        List<Map<String, Object>> roles = usuarioService.listarRoles();
        return ResponseEntity.ok(roles);
    }
}
