package com.example.rntn.service;

import com.example.rntn.entity.Permission;
import com.example.rntn.entity.Usuario;
import com.example.rntn.repository.PermissionRepository;
import com.example.rntn.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for checking user permissions
 * Implements permission-based authorization logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionService {

    private final UsuarioRepository usuarioRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Check if the current authenticated user has a specific permission
     *
     * @param permissionName Permission to check (e.g., "paciente:create")
     * @return true if user has permission, false otherwise
     */
    public boolean hasPermission(String permissionName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.debug("No authenticated user found");
            return false;
        }

        return hasPermission(auth.getName(), permissionName);
    }

    /**
     * Check if a specific user has a permission
     *
     * @param username Username to check
     * @param permissionName Permission to check
     * @return true if user has permission, false otherwise
     */
    public boolean hasPermission(String username, String permissionName) {
        try {
            Usuario user = usuarioRepository.findByNombreUsuarioWithRoles(username)
                .orElse(null);

            if (user == null) {
                log.warn("User not found: {}", username);
                return false;
            }

            // Check if any of the user's roles has the required permission
            boolean hasPermission = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(perm -> perm.getPermissionName().equals(permissionName));

            log.debug("Permission check - User: {}, Permission: {}, Result: {}",
                username, permissionName, hasPermission);

            return hasPermission;

        } catch (Exception e) {
            log.error("Error checking permission for user: {}, permission: {}",
                username, permissionName, e);
            return false;
        }
    }

    /**
     * Check if user has ANY of the specified permissions
     *
     * @param username Username to check
     * @param permissionNames Permissions to check (user needs at least one)
     * @return true if user has at least one permission, false otherwise
     */
    public boolean hasAnyPermission(String username, String... permissionNames) {
        if (permissionNames == null || permissionNames.length == 0) {
            return false;
        }

        for (String permissionName : permissionNames) {
            if (hasPermission(username, permissionName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if user has ALL of the specified permissions
     *
     * @param username Username to check
     * @param permissionNames Permissions to check (user needs all of them)
     * @return true if user has all permissions, false otherwise
     */
    public boolean hasAllPermissions(String username, String... permissionNames) {
        if (permissionNames == null || permissionNames.length == 0) {
            return true;
        }

        for (String permissionName : permissionNames) {
            if (!hasPermission(username, permissionName)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get all permissions for a user
     *
     * @param username Username
     * @return Set of permission names
     */
    public Set<String> getUserPermissions(String username) {
        Usuario user = usuarioRepository.findByNombreUsuarioWithRoles(username)
            .orElse(null);

        if (user == null) {
            return Set.of();
        }

        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(Permission::getPermissionName)
            .collect(Collectors.toSet());
    }

    /**
     * Get all permissions grouped by resource
     *
     * @param username Username
     * @return Map of resource -> list of actions
     */
    public java.util.Map<String, List<String>> getUserPermissionsByResource(String username) {
        Usuario user = usuarioRepository.findByNombreUsuarioWithRoles(username)
            .orElse(null);

        if (user == null) {
            return java.util.Map.of();
        }

        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .collect(Collectors.groupingBy(
                Permission::getResource,
                Collectors.mapping(Permission::getAction, Collectors.toList())
            ));
    }

    /**
     * Get all available permissions in the system
     *
     * @return List of all permissions
     */
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     * Get permissions by resource
     *
     * @param resource Resource name (e.g., "PACIENTE")
     * @return List of permissions for that resource
     */
    public List<Permission> getPermissionsByResource(String resource) {
        return permissionRepository.findByResource(resource);
    }

    /**
     * Get all unique resources in the system
     *
     * @return List of resource names
     */
    public List<String> getAllResources() {
        return permissionRepository.findAllDistinctResources();
    }

    /**
     * Get all unique actions in the system
     *
     * @return List of action names
     */
    public List<String> getAllActions() {
        return permissionRepository.findAllDistinctActions();
    }
}

