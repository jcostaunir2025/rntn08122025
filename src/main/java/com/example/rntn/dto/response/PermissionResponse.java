package com.example.rntn.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Permission
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con información de un permiso")
public class PermissionResponse {

    @Schema(description = "ID del permiso", example = "1")
    private Integer idPermission;

    @Schema(description = "Nombre único del permiso", example = "paciente:create")
    private String permissionName;

    @Schema(description = "Recurso al que aplica", example = "PACIENTE")
    private String resource;

    @Schema(description = "Acción permitida", example = "CREATE")
    private String action;

    @Schema(description = "Descripción del permiso", example = "Create new patient records")
    private String description;

    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;
}

