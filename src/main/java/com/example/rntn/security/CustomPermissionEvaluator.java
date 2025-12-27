package com.example.rntn.security;

import com.example.rntn.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom permission evaluator for Spring Security
 * Enables method-level security with @PreAuthorize("hasPermission(...)")
 *
 * Usage examples:
 * - @PreAuthorize("hasPermission(null, 'paciente:create')")
 * - @PreAuthorize("hasPermission(#id, 'PACIENTE', 'update')")
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final PermissionService permissionService;

    /**
     * Evaluate permission without target object
     * Used for: @PreAuthorize("hasPermission(null, 'paciente:create')")
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            log.debug("Authentication or permission is null");
            return false;
        }

        String username = authentication.getName();
        String permissionName = permission.toString();

        log.debug("Checking permission: {} for user: {}", permissionName, username);

        return permissionService.hasPermission(username, permissionName);
    }

    /**
     * Evaluate permission with target type and ID
     * Used for: @PreAuthorize("hasPermission(#id, 'PACIENTE', 'update')")
     *
     * This allows for future resource-level permissions (e.g., only update own records)
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                String targetType, Object permission) {
        if (authentication == null || targetType == null || permission == null) {
            log.debug("Authentication, targetType, or permission is null");
            return false;
        }

        String username = authentication.getName();

        // Construct permission name from targetType and permission
        // Format: resource:action (e.g., "paciente:update")
        String permissionName = targetType.toLowerCase() + ":" + permission.toString().toLowerCase();

        log.debug("Checking permission: {} for user: {} on resource ID: {}",
            permissionName, username, targetId);

        boolean hasPermission = permissionService.hasPermission(username, permissionName);

        // Future enhancement: Add resource-level checks here
        // For example: Check if user can only access their own records
        // if (targetId != null && !canAccessResource(username, targetType, targetId)) {
        //     return false;
        // }

        return hasPermission;
    }

    // Future method for resource-level authorization
    // private boolean canAccessResource(String username, String resourceType, Serializable resourceId) {
    //     // Implement logic to check if user can access specific resource
    //     // For example: doctors can only view their own patients
    //     return true;
    // }
}

