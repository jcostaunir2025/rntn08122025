package com.example.rntn.service;

import com.example.rntn.dto.request.UsuarioRequest;
import com.example.rntn.dto.response.UsuarioResponse;
import com.example.rntn.entity.Usuario;
import com.example.rntn.entity.UsuarioRoles;
import com.example.rntn.exception.ResourceNotFoundException;
import com.example.rntn.repository.UsuarioRepository;
import com.example.rntn.repository.UsuarioRolesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para Usuarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponse crearUsuario(UsuarioRequest request) {
        log.info("Creando usuario: {}", request.getNombreUsuario());

        if (usuarioRepository.existsByNombreUsuario(request.getNombreUsuario())) {
            throw new IllegalArgumentException("Ya existe un usuario con el nombre: " + request.getNombreUsuario());
        }

        // Obtener roles
        Set<UsuarioRoles> roles = new HashSet<>();
        for (Integer roleId : request.getRolesIds()) {
            UsuarioRoles rol = rolesRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleId));
            roles.add(rol);
        }

        // Hash password with BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassUsuario());
        log.debug("Password hashed successfully for user: {}", request.getNombreUsuario());

        Usuario usuario = Usuario.builder()
            .nombreUsuario(request.getNombreUsuario())
            .passUsuario(hashedPassword)
            .roles(roles)
            .build();

        usuario = usuarioRepository.save(usuario);

        return mapToResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));

        return mapToResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorNombre(String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByNombreUsuarioWithRoles(nombreUsuario)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + nombreUsuario));

        return mapToResponse(usuario);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listarUsuarios(Pageable pageable) {
        Page<Usuario> usuarios = usuarioRepository.findAll(pageable);
        return usuarios.map(this::mapToResponse);
    }

    public UsuarioResponse actualizarUsuario(Integer id, UsuarioRequest request) {
        log.info("Actualizando usuario: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));

        // Verificar nombre si cambió
        if (!usuario.getNombreUsuario().equals(request.getNombreUsuario())) {
            if (usuarioRepository.existsByNombreUsuario(request.getNombreUsuario())) {
                throw new IllegalArgumentException("Ya existe un usuario con el nombre: " + request.getNombreUsuario());
            }
        }

        // Obtener nuevos roles
        Set<UsuarioRoles> roles = new HashSet<>();
        for (Integer roleId : request.getRolesIds()) {
            UsuarioRoles rol = rolesRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleId));
            roles.add(rol);
        }

        usuario.setNombreUsuario(request.getNombreUsuario());

        // Hash password if it's being changed
        String hashedPassword = passwordEncoder.encode(request.getPassUsuario());
        usuario.setPassUsuario(hashedPassword);
        log.debug("Password updated and hashed for user: {}", usuario.getNombreUsuario());

        usuario.setRoles(roles);

        usuario = usuarioRepository.save(usuario);

        return mapToResponse(usuario);
    }

    public void eliminarUsuario(Integer id) {
        log.info("Eliminando usuario: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));

        usuarioRepository.delete(usuario);
    }

    /**
     * Listar todos los roles disponibles
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarRoles() {
        log.info("Listando roles disponibles");

        List<UsuarioRoles> roles = rolesRepository.findAll();

        return roles.stream()
            .map(rol -> {
                Map<String, Object> rolInfo = new java.util.HashMap<>();
                rolInfo.put("idRoles", rol.getIdRoles());
                rolInfo.put("permisosRoles", rol.getPermisosRoles());
                return rolInfo;
            })
            .collect(Collectors.toList());
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        List<UsuarioResponse.RolInfo> rolesInfo = usuario.getRoles().stream()
            .map(rol -> UsuarioResponse.RolInfo.builder()
                .idRoles(rol.getIdRoles())
                .permisosRoles(rol.getPermisosRoles())
                .build())
            .collect(Collectors.toList());

        return UsuarioResponse.builder()
            .idUsuario(usuario.getIdUsuario())
            .nombreUsuario(usuario.getNombreUsuario())
            .roles(rolesInfo)
            .createdAt(usuario.getCreatedAt())
            .updatedAt(usuario.getUpdatedAt())
            .build();
    }
}
