package com.example.rntn.controller;

import com.example.rntn.dto.response.PermissionResponse;
import com.example.rntn.entity.Permission;
import com.example.rntn.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller REST para consulta de permisos del sistema
 * Solo accesible por usuarios ADMIN
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Permissions", description = "API para consulta de permisos del sistema")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Listar todos los permisos disponibles en el sistema
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los permisos",
               description = "Retorna todos los permisos disponibles en el sistema")
    public ResponseEntity<List<PermissionResponse>> listarPermisos() {
        log.info("GET /api/v1/permissions - Listando todos los permisos");

        List<PermissionResponse> permissions = permissionService.getAllPermissions().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(permissions);
    }

    /**
     * Obtener un permiso específico por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener permiso por ID")
    public ResponseEntity<PermissionResponse> obtenerPermiso(
            @Parameter(description = "ID del permiso", required = true)
            @PathVariable Integer id) {

        log.info("GET /api/v1/permissions/{}", id);

        // This would require adding findById in PermissionService
        // For now, return not implemented
        return ResponseEntity.status(501).build();
    }

    /**
     * Listar permisos agrupados por recurso
     */
    @GetMapping("/by-resource")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar permisos agrupados por recurso",
               description = "Retorna los permisos organizados por recurso (PACIENTE, EVALUACION, etc.)")
    public ResponseEntity<Map<String, List<PermissionResponse>>> listarPermisosPorRecurso() {
        log.info("GET /api/v1/permissions/by-resource");

        Map<String, List<PermissionResponse>> permissionsByResource =
            permissionService.getAllPermissions().stream()
                .collect(Collectors.groupingBy(
                    Permission::getResource,
                    Collectors.mapping(this::mapToResponse, Collectors.toList())
                ));

        return ResponseEntity.ok(permissionsByResource);
    }

    /**
     * Listar todos los recursos disponibles
     */
    @GetMapping("/resources")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar recursos disponibles",
               description = "Retorna la lista de todos los recursos del sistema")
    public ResponseEntity<List<String>> listarRecursos() {
        log.info("GET /api/v1/permissions/resources");

        List<String> resources = permissionService.getAllResources();

        return ResponseEntity.ok(resources);
    }

    /**
     * Listar todas las acciones disponibles
     */
    @GetMapping("/actions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar acciones disponibles",
               description = "Retorna la lista de todas las acciones posibles (CREATE, READ, UPDATE, DELETE, etc.)")
    public ResponseEntity<List<String>> listarAcciones() {
        log.info("GET /api/v1/permissions/actions");

        List<String> actions = permissionService.getAllActions();

        return ResponseEntity.ok(actions);
    }

    /**
     * Obtener permisos del usuario autenticado
     */
    @GetMapping("/my-permissions")
    @Operation(summary = "Obtener mis permisos",
               description = "Retorna los permisos del usuario autenticado")
    public ResponseEntity<Set<String>> obtenerMisPermisos(Authentication authentication) {
        log.info("GET /api/v1/permissions/my-permissions - User: {}", authentication.getName());

        Set<String> userPermissions = permissionService.getUserPermissions(authentication.getName());

        return ResponseEntity.ok(userPermissions);
    }

    /**
     * Obtener permisos del usuario agrupados por recurso
     */
    @GetMapping("/my-permissions/by-resource")
    @Operation(summary = "Obtener mis permisos por recurso",
               description = "Retorna los permisos del usuario autenticado agrupados por recurso")
    public ResponseEntity<Map<String, List<String>>> obtenerMisPermisosPorRecurso(
            Authentication authentication) {

        log.info("GET /api/v1/permissions/my-permissions/by-resource - User: {}",
            authentication.getName());

        Map<String, List<String>> permissions =
            permissionService.getUserPermissionsByResource(authentication.getName());

        return ResponseEntity.ok(permissions);
    }

    /**
     * Verificar si el usuario tiene un permiso específico
     */
    @GetMapping("/check/{permissionName}")
    @Operation(summary = "Verificar permiso",
               description = "Verifica si el usuario autenticado tiene un permiso específico")
    public ResponseEntity<Map<String, Object>> verificarPermiso(
            @Parameter(description = "Nombre del permiso a verificar", example = "paciente:create")
            @PathVariable String permissionName,
            Authentication authentication) {

        log.info("GET /api/v1/permissions/check/{} - User: {}",
            permissionName, authentication.getName());

        boolean hasPermission = permissionService.hasPermission(
            authentication.getName(), permissionName);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("permission", permissionName);
        response.put("granted", hasPermission);

        return ResponseEntity.ok(response);
    }

    // Helper method
    private PermissionResponse mapToResponse(Permission permission) {
        return PermissionResponse.builder()
            .idPermission(permission.getIdPermission())
            .permissionName(permission.getPermissionName())
            .resource(permission.getResource())
            .action(permission.getAction())
            .description(permission.getDescription())
            .createdAt(permission.getCreatedAt())
            .build();
    }
}

