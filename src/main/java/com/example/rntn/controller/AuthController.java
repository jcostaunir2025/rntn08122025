package com.example.rntn.controller;

import com.example.rntn.dto.request.LoginRequest;
import com.example.rntn.dto.response.AuthResponse;
import com.example.rntn.entity.Permission;
import com.example.rntn.entity.Usuario;
import com.example.rntn.entity.UsuarioRoles;
import com.example.rntn.repository.UsuarioRepository;
import com.example.rntn.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller para autenticación y autorización
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para login y gestión de tokens JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @PostMapping("/login")
    @Operation(
        summary = "Autenticar usuario",
        description = "Autentica un usuario y retorna un token JWT para acceso a la API"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Autenticación exitosa",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Intento de login para usuario: {}", request.getUsername());

        // Autenticar usuario
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        // Obtener detalles del usuario autenticado
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Generar token JWT
        String token = jwtUtil.generateToken(userDetails);

        // Obtener usuario de la base de datos con roles y permisos
        Usuario usuario = usuarioRepository.findByNombreUsuarioWithRoles(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Extraer roles (sin el prefijo "ROLE_")
        List<String> roles = usuario.getRoles().stream()
                .map(UsuarioRoles::getPermisosRoles)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Extraer permisos de todos los roles
        List<String> permissions = usuario.getRoles().stream()
                .flatMap(rol -> rol.getPermissions().stream())
                .map(Permission::getPermissionName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .username(userDetails.getUsername())
                .roles(roles)
                .permissions(permissions)
                .expiresIn(jwtExpiration)
                .build();

        log.info("Login exitoso para usuario: {} - Roles: {} - Permisos: {}",
                request.getUsername(), roles.size(), permissions.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @Operation(
        summary = "Validar token JWT",
        description = "Verifica si el token JWT proporcionado es válido"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token válido"),
        @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String username = jwtUtil.extractUsername(token);
                return ResponseEntity.ok("Token válido para usuario: " + username);
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Token inválido: " + e.getMessage());
            }
        }
        return ResponseEntity.status(401).body("No se proporcionó token");
    }
}

