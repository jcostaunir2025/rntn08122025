package com.example.rntn.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Paciente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con información de un paciente")
public class PacienteResponse {

    @Schema(description = "ID del paciente", example = "1")
    private Integer idPaciente;

    @Schema(description = "Documento del paciente", example = "12345678")
    private String docPaciente;

    @Schema(description = "Nombre del paciente", example = "Juan Pérez García")
    private String nombrePaciente;

    @Schema(description = "Dirección del paciente")
    private String direccionPaciente;

    @Schema(description = "Email del paciente")
    private String emailPaciente;

    @Schema(description = "Teléfono del paciente")
    private String telefonoPaciente;

    @Schema(description = "Estado del paciente", example = "ACTIVO")
    private String estatusPaciente;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de última actualización")
    private LocalDateTime updatedAt;
}

