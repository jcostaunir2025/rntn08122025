package com.example.rntn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para predicción de sentimiento individual
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para análisis de sentimiento de un texto individual")
public class PredictRequest {

    @NotBlank(message = "El texto no puede estar vacío")
    @Size(min = 1, max = 5000, message = "El texto debe tener entre 1 y 5000 caracteres")
    @Schema(description = "Texto a analizar", example = "Me siento muy ansioso últimamente", required = true)
    private String text;

    @Schema(description = "Ruta al modelo (opcional, usa modelo por defecto)", example = "models/out-model.ser.gz")
    private String modelPath;
}

