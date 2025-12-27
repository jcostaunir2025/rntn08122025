package com.example.rntn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO de request para asignar permisos a un rol
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para asignar permisos a un rol")
public class RolePermissionsRequest {

    @NotNull(message = "El ID del rol es obligatorio")
    @Schema(description = "ID del rol", example = "2", required = true)
    private Integer roleId;

    @NotNull(message = "La lista de IDs de permisos es obligatoria")
    @Schema(description = "Lista de IDs de permisos a asignar", example = "[1, 2, 3]", required = true)
    private Set<Integer> permissionIds;
}

