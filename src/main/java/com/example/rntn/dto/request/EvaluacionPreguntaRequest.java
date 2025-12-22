package com.example.rntn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para crear/actualizar EvaluacionPregunta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para crear o actualizar una pregunta de evaluación")
public class EvaluacionPreguntaRequest {

    @NotBlank(message = "El texto de la pregunta es obligatorio")
    @Size(min = 5, max = 1000, message = "La pregunta debe tener entre 5 y 1000 caracteres")
    @Schema(description = "Texto de la pregunta",
            example = "¿Cómo se siente hoy?",
            required = true)
    private String textoEvaluacionPregunta;
}

