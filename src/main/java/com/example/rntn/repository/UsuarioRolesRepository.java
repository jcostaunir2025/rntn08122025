package com.example.rntn.repository;

import com.example.rntn.entity.UsuarioRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para UsuarioRoles
 */
@Repository
public interface UsuarioRolesRepository extends JpaRepository<UsuarioRoles, Integer> {

    /**
     * Busca un rol por su nombre de permisos
     */
    Optional<UsuarioRoles> findByPermisosRoles(String permisos);
}

