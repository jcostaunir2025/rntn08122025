package com.example.rntn.repository;

import com.example.rntn.entity.Reporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repositorio JPA para Reporte
 */
@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {

    /**
     * Obtiene reportes de un usuario específico
     */
    Page<Reporte> findByUsuarioIdUsuario(Integer idUsuario, Pageable pageable);

    /**
     * Obtiene reportes de una evaluación específica
     */
    Page<Reporte> findByEvaluacionIdEvaluacion(Integer idEvaluacion, Pageable pageable);

    /**
     * Reportes de un usuario en un rango de fechas
     */
    @Query("SELECT r FROM Reporte r WHERE " +
           "r.usuario.idUsuario = :idUsuario AND " +
           "r.fechageneracionReporte BETWEEN :desde AND :hasta")
    Page<Reporte> findByUsuarioAndDateRange(
        @Param("idUsuario") Integer idUsuario,
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta,
        Pageable pageable
    );

    /**
     * Obtiene un reporte con todos sus detalles (JOIN FETCH)
     */
    @Query("SELECT r FROM Reporte r " +
           "JOIN FETCH r.evaluacion e " +
           "JOIN FETCH e.consulta c " +
           "WHERE r.idReporte = :id")
    Reporte findByIdWithFullDetails(@Param("id") Integer id);
}

