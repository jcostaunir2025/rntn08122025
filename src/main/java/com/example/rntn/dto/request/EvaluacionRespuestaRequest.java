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
 * DTO de request para registrar una respuesta de evaluación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para registrar una respuesta de evaluación con análisis de sentimiento")
public class EvaluacionRespuestaRequest {

    @NotNull(message = "El ID de la pregunta es obligatorio")
    @Schema(description = "ID de la pregunta de evaluación", example = "1", required = true)
    private Integer idEvaluacionPregunta;

    @NotBlank(message = "El texto de la respuesta no puede estar vacío")
    @Size(min = 1, max = 5000, message = "La respuesta debe tener entre 1 y 5000 caracteres")
    @Schema(description = "Texto de la respuesta del paciente",
            example = "Me siento muy ansioso y no puedo dormir bien",
            required = true)
    private String textoEvaluacionRespuesta;

    @Schema(description = "Indica si se debe analizar el sentimiento automáticamente",
            example = "true",
            defaultValue = "true")
    @Builder.Default
    private boolean analizarSentimiento = true;
}

