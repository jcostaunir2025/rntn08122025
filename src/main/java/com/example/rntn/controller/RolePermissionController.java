package com.example.rntn.controller;

import com.example.rntn.dto.request.RolePermissionsRequest;
import com.example.rntn.dto.response.PermissionResponse;
import com.example.rntn.entity.Permission;
import com.example.rntn.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller REST para gestión de asignación de permisos a roles
 * Solo accesible por usuarios ADMIN
 */
@RestController
@RequestMapping("/api/v1/role-permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Permissions", description = "API para gestión de permisos por rol")
@PreAuthorize("hasRole('ADMIN')")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    /**
     * Obtener todos los permisos de un rol
     */
    @GetMapping("/role/{roleId}")
    @Operation(summary = "Obtener permisos de un rol",
               description = "Retorna todos los permisos asignados a un rol específico")
    public ResponseEntity<List<PermissionResponse>> obtenerPermisosDeRol(
            @Parameter(description = "ID del rol", required = true)
            @PathVariable Integer roleId) {

        log.info("GET /api/v1/role-permissions/role/{}", roleId);

        Set<Permission> permissions = rolePermissionService.getRolePermissions(roleId);

        List<PermissionResponse> response = permissions.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Asignar permisos a un rol (reemplaza permisos existentes)
     */
    @PutMapping("/assign")
    @Operation(summary = "Asignar permisos a rol",
               description = "Asigna permisos a un rol, reemplazando los permisos existentes")
    public ResponseEntity<Map<String, String>> asignarPermisos(
            @Valid @RequestBody RolePermissionsRequest request) {

        log.info("PUT /api/v1/role-permissions/assign - Role: {}, Permissions: {}",
            request.getRoleId(), request.getPermissionIds().size());

        rolePermissionService.assignPermissionsToRole(request);

        return ResponseEntity.ok(Map.of(
            "message", "Permisos asignados correctamente",
            "roleId", request.getRoleId().toString(),
            "permissionCount", String.valueOf(request.getPermissionIds().size())
        ));
    }

    /**
     * Agregar permisos a un rol (mantiene permisos existentes)
     */
    @PostMapping("/role/{roleId}/add")
    @Operation(summary = "Agregar permisos a rol",
               description = "Agrega nuevos permisos a un rol sin eliminar los existentes")
    public ResponseEntity<Map<String, String>> agregarPermisos(
            @Parameter(description = "ID del rol", required = true)
            @PathVariable Integer roleId,
            @RequestBody Set<Integer> permissionIds) {

        log.info("POST /api/v1/role-permissions/role/{}/add - Permissions: {}",
            roleId, permissionIds.size());

        rolePermissionService.addPermissionsToRole(roleId, permissionIds);

        return ResponseEntity.ok(Map.of(
            "message", "Permisos agregados correctamente",
            "roleId", roleId.toString(),
            "addedCount", String.valueOf(permissionIds.size())
        ));
    }

    /**
     * Remover permisos de un rol
     */
    @DeleteMapping("/role/{roleId}/remove")
    @Operation(summary = "Remover permisos de rol",
               description = "Remueve permisos específicos de un rol")
    public ResponseEntity<Map<String, String>> removerPermisos(
            @Parameter(description = "ID del rol", required = true)
            @PathVariable Integer roleId,
            @RequestBody Set<Integer> permissionIds) {

        log.info("DELETE /api/v1/role-permissions/role/{}/remove - Permissions: {}",
            roleId, permissionIds.size());

        rolePermissionService.removePermissionsFromRole(roleId, permissionIds);

        return ResponseEntity.ok(Map.of(
            "message", "Permisos removidos correctamente",
            "roleId", roleId.toString(),
            "removedCount", String.valueOf(permissionIds.size())
        ));
    }

    /**
     * Obtener resumen de permisos de todos los roles
     */
    @GetMapping("/summary")
    @Operation(summary = "Resumen de permisos por rol",
               description = "Retorna un resumen de permisos asignados a cada rol")
    public ResponseEntity<List<Map<String, Object>>> obtenerResumenPermisos() {
        log.info("GET /api/v1/role-permissions/summary");

        List<Map<String, Object>> summary = rolePermissionService.getRolePermissionSummary();

        return ResponseEntity.ok(summary);
    }

    /**
     * Verificar si un rol tiene un permiso específico
     */
    @GetMapping("/role/{roleId}/has/{permissionName}")
    @Operation(summary = "Verificar permiso de rol",
               description = "Verifica si un rol tiene un permiso específico")
    public ResponseEntity<Map<String, Object>> verificarPermisoDeRol(
            @Parameter(description = "ID del rol", required = true)
            @PathVariable Integer roleId,
            @Parameter(description = "Nombre del permiso", example = "paciente:create")
            @PathVariable String permissionName) {

        log.info("GET /api/v1/role-permissions/role/{}/has/{}", roleId, permissionName);

        boolean hasPermission = rolePermissionService.roleHasPermission(roleId, permissionName);

        return ResponseEntity.ok(Map.of(
            "roleId", roleId,
            "permission", permissionName,
            "granted", hasPermission
        ));
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

