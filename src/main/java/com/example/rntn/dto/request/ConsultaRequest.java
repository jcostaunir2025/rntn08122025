package com.example.rntn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para crear una consulta médica")
public class ConsultaRequest {

    @NotNull(message = "El ID del paciente es obligatorio")
    @Schema(description = "ID del paciente", example = "1", required = true)
    private Integer idPaciente;

    @NotNull(message = "El ID del personal es obligatorio")
    @Schema(description = "ID del personal médico", example = "1", required = true)
    private Integer idPersonal;

    @Schema(description = "ID de la evaluación asociada (opcional, N:1 relationship)", example = "1")
    private Integer idEvaluacion;

    @NotNull(message = "La fecha/hora de la consulta es obligatoria")
    @FutureOrPresent(message = "La fecha/hora debe ser presente o futura")
    @Schema(description = "Fecha y hora de la consulta", example = "2025-12-21T15:00:00", required = true)
    private LocalDateTime fechahoraConsulta;

    @Schema(description = "ID del estado de la consulta (1=PENDIENTE, 2=EN_PROGRESO, 3=COMPLETADA, 4=CANCELADA, 5=REPROGRAMADA, 6=NO_ASISTIO)",
            example = "1",
            defaultValue = "1")
    @Builder.Default
    private Integer estatusConsulta = 1; // Default: PENDIENTE
}

