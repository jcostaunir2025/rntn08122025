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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para crear o actualizar personal médico")
public class PersonalRequest {

    @NotBlank(message = "El documento del personal es obligatorio")
    @Size(max = 20, message = "El documento no puede exceder 20 caracteres")
    @Schema(description = "Documento de identificación", example = "DOC-001", required = true)
    private String docPersonal;

    @NotBlank(message = "El nombre del personal es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Schema(description = "Nombre completo", example = "Dra. María García López", required = true)
    private String nombrePersonal;

    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Schema(description = "Email de contacto", example = "maria.garcia@hospital.com")
    private String emailPersonal;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Schema(description = "Teléfono de contacto", example = "+34 666 111 222")
    private String telefonoPersonal;

    @Schema(description = "ID del usuario del sistema (1:1 relationship)", example = "1")
    private Integer idUsuario;

    @Pattern(regexp = "ACTIVO|INACTIVO", message = "El estatus debe ser ACTIVO o INACTIVO")
    @Schema(description = "Estado del personal", example = "ACTIVO", allowableValues = {"ACTIVO", "INACTIVO"})
    @Builder.Default
    private String estatusPersonal = "ACTIVO";
}

