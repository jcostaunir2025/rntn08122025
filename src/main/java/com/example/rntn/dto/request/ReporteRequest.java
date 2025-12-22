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
 * DTO de request para crear Reporte
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para crear un reporte")
public class ReporteRequest {

    @NotNull(message = "El ID del usuario es obligatorio")
    @Schema(description = "ID del usuario que genera el reporte", example = "1", required = true)
    private Integer idUsuario;

    @NotNull(message = "El ID de la evaluación es obligatorio")
    @Schema(description = "ID de la evaluación sobre la cual generar el reporte", example = "1", required = true)
    private Integer idEvaluacion;

    @NotBlank(message = "El nombre del reporte es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Schema(description = "Nombre descriptivo del reporte",
            example = "Reporte de Análisis de Sentimientos - Sesión 1",
            required = true)
    private String nombreReporte;

    @Size(max = 5000, message = "El resultado no puede exceder 5000 caracteres")
    @Schema(description = "Contenido del reporte o conclusiones",
            example = "El paciente muestra signos de ansiedad moderada...")
    private String resultadoReporte;
}

