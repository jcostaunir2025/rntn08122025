package com.example.rntn.repository;

import com.example.rntn.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for Permission entity operations
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    /**
     * Find permission by its unique name
     */
    Optional<Permission> findByPermissionName(String permissionName);

    /**
     * Find all permissions for a specific resource
     */
    List<Permission> findByResource(String resource);

    /**
     * Find all permissions for a specific action
     */
    List<Permission> findByAction(String action);

    /**
     * Find all permissions for a resource and action combination
     */
    List<Permission> findByResourceAndAction(String resource, String action);

    /**
     * Check if a permission exists by name
     */
    boolean existsByPermissionName(String permissionName);

    /**
     * Find all permissions by their names
     */
    @Query("SELECT p FROM Permission p WHERE p.permissionName IN :permissionNames")
    Set<Permission> findByPermissionNameIn(@Param("permissionNames") Set<String> permissionNames);

    /**
     * Get all unique resources
     */
    @Query("SELECT DISTINCT p.resource FROM Permission p ORDER BY p.resource")
    List<String> findAllDistinctResources();

    /**
     * Get all unique actions
     */
    @Query("SELECT DISTINCT p.action FROM Permission p ORDER BY p.action")
    List<String> findAllDistinctActions();
}

