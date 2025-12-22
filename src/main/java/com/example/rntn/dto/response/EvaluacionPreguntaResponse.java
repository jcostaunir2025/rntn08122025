package com.example.rntn.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para EvaluacionPregunta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con información de una pregunta de evaluación")
public class EvaluacionPreguntaResponse {

    @Schema(description = "ID de la pregunta", example = "1")
    private Integer idEvaluacionPregunta;

    @Schema(description = "Texto de la pregunta", example = "¿Cómo se siente hoy?")
    private String textoEvaluacionPregunta;

    @Schema(description = "Cantidad de respuestas asociadas", example = "15")
    private Integer cantidadRespuestas;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;
}

