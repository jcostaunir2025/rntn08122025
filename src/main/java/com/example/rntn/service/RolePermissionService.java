package com.example.rntn.service;

import com.example.rntn.dto.request.RolePermissionsRequest;
import com.example.rntn.entity.Permission;
import com.example.rntn.entity.UsuarioRoles;
import com.example.rntn.exception.ResourceNotFoundException;
import com.example.rntn.repository.PermissionRepository;
import com.example.rntn.repository.UsuarioRolesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing role-permission assignments
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RolePermissionService {

    private final UsuarioRolesRepository rolesRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Get all permissions assigned to a role
     */
    @Transactional(readOnly = true)
    public Set<Permission> getRolePermissions(Integer roleId) {
        UsuarioRoles role = rolesRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleId));

        return role.getPermissions();
    }

    /**
     * Assign permissions to a role (replaces existing permissions)
     */
    public void assignPermissionsToRole(RolePermissionsRequest request) {
        log.info("Asignando permisos al rol: {}", request.getRoleId());

        UsuarioRoles role = rolesRepository.findById(request.getRoleId())
            .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + request.getRoleId()));

        // Find all permissions by IDs
        Set<Permission> permissions = new HashSet<>();
        for (Integer permId : request.getPermissionIds()) {
            Permission permission = permissionRepository.findById(permId)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado: " + permId));
            permissions.add(permission);
        }

        // Replace existing permissions
        role.setPermissions(permissions);
        rolesRepository.save(role);

        log.info("Asignados {} permisos al rol {}", permissions.size(), role.getPermisosRoles());
    }

    /**
     * Add permissions to a role (keeps existing permissions)
     */
    public void addPermissionsToRole(Integer roleId, Set<Integer> permissionIds) {
        log.info("Agregando permisos al rol: {}", roleId);

        UsuarioRoles role = rolesRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleId));

        Set<Permission> existingPermissions = role.getPermissions();

        // Add new permissions
        for (Integer permId : permissionIds) {
            Permission permission = permissionRepository.findById(permId)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado: " + permId));
            existingPermissions.add(permission);
        }

        role.setPermissions(existingPermissions);
        rolesRepository.save(role);

        log.info("Agregados permisos al rol {}. Total: {}",
            role.getPermisosRoles(), existingPermissions.size());
    }

    /**
     * Remove permissions from a role
     */
    public void removePermissionsFromRole(Integer roleId, Set<Integer> permissionIds) {
        log.info("Removiendo permisos del rol: {}", roleId);

        UsuarioRoles role = rolesRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleId));

        Set<Permission> permissions = role.getPermissions();
        permissions.removeIf(perm -> permissionIds.contains(perm.getIdPermission()));

        role.setPermissions(permissions);
        rolesRepository.save(role);

        log.info("Removidos permisos del rol {}. Restantes: {}",
            role.getPermisosRoles(), permissions.size());
    }

    /**
     * Get permission summary for all roles
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRolePermissionSummary() {
        List<UsuarioRoles> allRoles = rolesRepository.findAll();

        return allRoles.stream()
            .map(role -> {
                Map<String, Object> summary = new java.util.HashMap<>();
                summary.put("roleId", role.getIdRoles());
                summary.put("roleName", role.getPermisosRoles());
                summary.put("permissionCount", role.getPermissions().size());
                summary.put("permissions", role.getPermissions().stream()
                    .map(Permission::getPermissionName)
                    .sorted()
                    .collect(Collectors.toList()));
                return summary;
            })
            .collect(Collectors.toList());
    }

    /**
     * Check if a role has a specific permission
     */
    @Transactional(readOnly = true)
    public boolean roleHasPermission(Integer roleId, String permissionName) {
        UsuarioRoles role = rolesRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleId));

        return role.getPermissions().stream()
            .anyMatch(perm -> perm.getPermissionName().equals(permissionName));
    }
}

