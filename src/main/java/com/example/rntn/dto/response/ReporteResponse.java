package com.example.rntn.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Reporte
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con información de un reporte")
public class ReporteResponse {

    @Schema(description = "ID del reporte", example = "1")
    private Integer idReporte;

    @Schema(description = "Información del usuario que generó el reporte")
    private UsuarioInfo usuario;

    @Schema(description = "Información de la evaluación")
    private EvaluacionInfo evaluacion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de generación del reporte")
    private LocalDateTime fechageneracionReporte;

    @Schema(description = "Nombre del reporte")
    private String nombreReporte;

    @Schema(description = "Resultado o contenido del reporte")
    private String resultadoReporte;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioInfo {
        private Integer idUsuario;
        private String nombreUsuario;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluacionInfo {
        private Integer idEvaluacion;
        private String nombreEvaluacion;
        private String tituloEvaluacion;
    }
}

