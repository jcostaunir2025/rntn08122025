package com.example.rntn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para crear/actualizar Paciente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para crear o actualizar un paciente")
public class PacienteRequest {

    @NotBlank(message = "El documento del paciente es obligatorio")
    @Size(max = 20, message = "El documento no puede exceder 20 caracteres")
    @Schema(description = "Documento de identificación del paciente", example = "12345678", required = true)
    private String docPaciente;

    @NotBlank(message = "El nombre del paciente es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Schema(description = "Nombre completo del paciente", example = "Juan Pérez García", required = true)
    private String nombrePaciente;

    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    @Schema(description = "Dirección del paciente", example = "Calle Principal 123, Madrid")
    private String direccionPaciente;

    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Schema(description = "Email del paciente", example = "juan.perez@example.com")
    private String emailPaciente;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "El teléfono solo puede contener números y caracteres + - ( ) espacio")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Schema(description = "Teléfono del paciente", example = "+34 666 777 888")
    private String telefonoPaciente;

    @Pattern(regexp = "ACTIVO|INACTIVO|SUSPENDIDO", message = "El estatus debe ser ACTIVO, INACTIVO o SUSPENDIDO")
    @Schema(description = "Estado del paciente", example = "ACTIVO", allowableValues = {"ACTIVO", "INACTIVO", "SUSPENDIDO"})
    @Builder.Default
    private String estatusPaciente = "ACTIVO";
}

