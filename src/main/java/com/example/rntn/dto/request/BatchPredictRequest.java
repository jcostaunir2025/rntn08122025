package com.example.rntn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de request para predicción de sentimiento por lote
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para análisis de sentimiento de múltiples textos")
public class BatchPredictRequest {

    @NotEmpty(message = "La lista de textos no puede estar vacía")
    @Size(min = 1, max = 100, message = "La lista debe contener entre 1 y 100 textos")
    @Schema(description = "Lista de textos a analizar", required = true)
    private List<String> texts;

    @Schema(description = "Ruta al modelo (opcional)", example = "models/out-model.ser.gz")
    private String modelPath;
}

