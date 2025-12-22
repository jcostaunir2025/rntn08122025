package com.example.rntn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para crear/actualizar Evaluacion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para crear o actualizar una evaluación")
public class EvaluacionRequest {

    @NotNull(message = "El ID de la consulta es obligatorio")
    @Schema(description = "ID de la consulta asociada", example = "1", required = true)
    private Integer idConsulta;

    @NotBlank(message = "El nombre de la evaluación es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Schema(description = "Nombre de la evaluación",
            example = "Evaluación de Sentimientos - Sesión 1",
            required = true)
    private String nombreEvaluacion;

    @Size(max = 50, message = "El área no puede exceder 50 caracteres")
    @Schema(description = "Área de evaluación", example = "SALUD_MENTAL")
    private String areaEvaluacion;
}

