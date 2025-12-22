package com.example.rntn.repository;

import com.example.rntn.entity.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Paciente
 * Proporciona operaciones CRUD y consultas personalizadas
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

    /**
     * Busca un paciente por su documento de identificación
     */
    Optional<Paciente> findByDocPaciente(String docPaciente);

    /**
     * Obtiene pacientes por estatus con paginación
     */
    Page<Paciente> findByEstatusPaciente(String estatus, Pageable pageable);

    /**
     * Búsqueda de pacientes por nombre o documento (like)
     */
    @Query("SELECT p FROM Paciente p WHERE " +
           "LOWER(p.nombrePaciente) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.docPaciente) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Paciente> searchPacientes(@Param("search") String search, Pageable pageable);

    /**
     * Verifica si existe un paciente con el documento dado
     */
    boolean existsByDocPaciente(String docPaciente);
}

