package com.example.rntn.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para análisis de sentimiento RNTN
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado del análisis de sentimiento usando modelo RNTN")
public class AnalisisSentimientoResponse {

    @Schema(description = "Texto analizado", example = "Me siento muy ansioso últimamente")
    private String texto;

    @Schema(description = "Clase predicha por el modelo (0-4)", example = "0")
    private Integer predictedClass;

    @Schema(description = "Label del sentimiento",
            example = "ANXIETY",
            allowableValues = {"ANXIETY", "SUICIDAL", "ANGER", "SADNESS", "FRUSTRATION"})
    private String predictedLabel;

    @Schema(description = "Nivel de confianza del modelo (0.0 - 1.0)", example = "0.92")
    private Double confidence;

    @Schema(description = "Nivel de riesgo",
            example = "MEDIO",
            allowableValues = {"BAJO", "MEDIO", "ALTO"})
    private String nivelRiesgo;

    @Schema(description = "Timestamp del análisis")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}

