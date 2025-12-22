package com.example.rntn.repository;

import com.example.rntn.entity.Consulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad Consulta
 * Incluye consultas optimizadas para reportes y dashboard
 */
@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Integer> {

    /**
     * Obtiene consultas de un paciente específico
     */
    Page<Consulta> findByPacienteIdPaciente(Integer idPaciente, Pageable pageable);

    /**
     * Obtiene consultas atendidas por un personal específico
     */
    Page<Consulta> findByPersonalIdPersonal(Integer idPersonal, Pageable pageable);

    /**
     * Obtiene consultas por estado
     */
    Page<Consulta> findByEstatusConsulta(String estatus, Pageable pageable);

    /**
     * Consultas de un paciente en un rango de fechas
     */
    @Query("SELECT c FROM Consulta c WHERE " +
           "c.paciente.idPaciente = :idPaciente AND " +
           "c.fechahoraConsulta BETWEEN :desde AND :hasta")
    Page<Consulta> findByPacienteAndDateRange(
        @Param("idPaciente") Integer idPaciente,
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta,
        Pageable pageable
    );

    /**
     * Obtiene una consulta con sus evaluaciones (JOIN FETCH para evitar N+1)
     */
    @Query("SELECT c FROM Consulta c LEFT JOIN FETCH c.evaluaciones WHERE c.idConsulta = :id")
    Consulta findByIdWithEvaluaciones(@Param("id") Integer id);

    /**
     * Consultas en un rango de fechas (sin paginación)
     */
    List<Consulta> findByFechahoraConsultaBetween(LocalDateTime desde, LocalDateTime hasta);
}

