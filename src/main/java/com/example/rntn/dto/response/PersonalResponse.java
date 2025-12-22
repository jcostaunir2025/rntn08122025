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
@Schema(description = "Respuesta con información del personal médico")
public class PersonalResponse {

    @Schema(description = "ID del personal", example = "1")
    private Integer idPersonal;

    @Schema(description = "Documento del personal", example = "DOC-001")
    private String docPersonal;

    @Schema(description = "Nombre del personal", example = "Dra. María García López")
    private String nombrePersonal;

    @Schema(description = "Email del personal", example = "maria.garcia@hospital.com")
    private String emailPersonal;

    @Schema(description = "Teléfono del personal", example = "+34 666 111 222")
    private String telefonoPersonal;

    @Schema(description = "Estado del personal", example = "ACTIVO")
    private String estatusPersonal;

    @Schema(description = "ID del usuario del sistema (1:1 relationship)", example = "1")
    private Integer idUsuario;

    @Schema(description = "Nombre de usuario del sistema", example = "dra.garcia")
    private String nombreUsuario;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de última actualización")
    private LocalDateTime updatedAt;
}

