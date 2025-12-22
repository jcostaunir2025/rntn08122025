package com.example.rntn.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response para login exitoso con token JWT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response de autenticación con token JWT")
public class AuthResponse {

    @Schema(description = "Token JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Tipo de token", example = "Bearer")
    @Builder.Default
    private String type = "Bearer";

    @Schema(description = "Nombre de usuario", example = "admin")
    private String username;

    @Schema(description = "Roles del usuario", example = "[\"ROLE_ADMIN\"]")
    private List<String> roles;

    @Schema(description = "Tiempo de expiración en milisegundos", example = "3600000")
    private Long expiresIn;
}

