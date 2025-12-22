package com.example.rntn.repository;

import com.example.rntn.entity.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para Evaluacion
 */
@Repository
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Integer> {

    /**
     * Obtiene todas las evaluaciones de una consulta
     */
    List<Evaluacion> findByConsultaIdConsulta(Integer idConsulta);

    /**
     * Obtiene una evaluación con sus reportes asociados
     */
    @Query("SELECT e FROM Evaluacion e LEFT JOIN FETCH e.reportes WHERE e.idEvaluacion = :id")
    Evaluacion findByIdWithReportes(@Param("id") Integer id);
}

