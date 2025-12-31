package com.example.rntn.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con información de una consulta")
public class ConsultaResponse {

    @Schema(description = "ID de la consulta")
    private Integer idConsulta;

    @Schema(description = "Información básica del paciente")
    private PacienteBasicInfo paciente;

    @Schema(description = "Información básica del personal")
    private PersonalBasicInfo personal;

    @Schema(description = "ID de la evaluación asociada")
    private Integer idEvaluacion;

    @Schema(description = "Información básica de la evaluación")
    private EvaluacionBasicInfo evaluacion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha y hora de inicio de la consulta")
    private LocalDateTime fechahoraConsulta;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha y hora de finalización de la consulta")
    private LocalDateTime fechafinConsulta;

    @Schema(description = "ID del estado de la consulta", example = "1")
    private Integer estatusConsulta;

    @Schema(description = "Nombre del estado de la consulta", example = "PENDIENTE")
    private String estatusConsultaNombre;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PacienteBasicInfo {
        private Integer idPaciente;
        private String nombrePaciente;
        private String docPaciente;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalBasicInfo {
        private Integer idPersonal;
        private String nombrePersonal;
        private String docPersonal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluacionBasicInfo {
        private Integer idEvaluacion;
        private String nombreEvaluacion;
        private String tituloEvaluacion;
    }
}

