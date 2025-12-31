package com.example.rntn.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para Evaluacion
 * ⭐ UPDATED: Removed idConsulta and paciente info - Relationship is now in Consulta (N:1)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con información de una evaluación")
public class EvaluacionResponse {

    @Schema(description = "ID de la evaluación", example = "1")
    private Integer idEvaluacion;

    @Schema(description = "Título de la evaluación")
    private String tituloEvaluacion;

    @Schema(description = "Nombre de la evaluación")
    private String nombreEvaluacion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de la evaluación")
    private LocalDateTime fechaEvaluacion;

    @Schema(description = "Área de evaluación")
    private String areaEvaluacion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de última actualización")
    private LocalDateTime updatedAt;
}

