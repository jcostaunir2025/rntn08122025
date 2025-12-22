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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con información de una evaluación")
public class EvaluacionResponse {

    @Schema(description = "ID de la evaluación", example = "1")
    private Integer idEvaluacion;

    @Schema(description = "ID de la consulta asociada")
    private Integer idConsulta;

    @Schema(description = "Nombre de la evaluación")
    private String nombreEvaluacion;

    @Schema(description = "Área de evaluación")
    private String areaEvaluacion;

    @Schema(description = "Información del paciente")
    private PacienteInfo paciente;

    @Schema(description = "Cantidad de respuestas registradas")
    private Integer cantidadRespuestas;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de última actualización")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PacienteInfo {
        private Integer idPaciente;
        private String nombrePaciente;
    }
}

