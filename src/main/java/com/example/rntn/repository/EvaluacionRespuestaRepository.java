package com.example.rntn.repository;

import com.example.rntn.entity.EvaluacionRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Repositorio JPA para EvaluacionRespuesta
 * ⭐ CLAVE para integración RNTN - almacena análisis de sentimientos
 */
@Repository
public interface EvaluacionRespuestaRepository extends JpaRepository<EvaluacionRespuesta, Integer> {

    /**
     * Obtiene todas las respuestas de una pregunta específica
     */
    List<EvaluacionRespuesta> findByEvaluacionPreguntaIdEvaluacionPregunta(Integer idPregunta);

    /**
     * Busca respuestas por label de sentimiento (ANXIETY, SUICIDAL, etc.)
     */
    @Query("SELECT er FROM EvaluacionRespuesta er WHERE er.labelEvaluacionRespuesta = :label")
    List<EvaluacionRespuesta> findByLabel(@Param("label") String label);

    /**
     * Cuenta respuestas agrupadas por label para un conjunto de preguntas
     * Útil para generar reportes y estadísticas
     */
    @Query("SELECT er.labelEvaluacionRespuesta, COUNT(er) FROM EvaluacionRespuesta er " +
           "WHERE er.evaluacionPregunta.idEvaluacionPregunta IN :preguntaIds " +
           "GROUP BY er.labelEvaluacionRespuesta")
    List<Object[]> countByLabelForPreguntas(@Param("preguntaIds") List<Integer> preguntaIds);

    /**
     * Busca respuestas con riesgo alto (SUICIDAL con confianza > umbral)
     */
    @Query("SELECT er FROM EvaluacionRespuesta er " +
           "WHERE er.labelEvaluacionRespuesta = 'SUICIDAL' " +
           "AND er.confidenceScore > :threshold " +
           "ORDER BY er.confidenceScore DESC")
    List<EvaluacionRespuesta> findHighRiskResponses(@Param("threshold") Double threshold);

    /**
     * Llama al stored procedure para obtener estadísticas agregadas
     * @param respuestaIds IDs separados por coma: "1,2,3,4,5"
     */
    @Query(value = "CALL sp_get_sentiment_aggregate_stats(:respuestaIds)", nativeQuery = true)
    List<Map<String, Object>> getAggregateStats(@Param("respuestaIds") String respuestaIds);

    /**
     * Obtiene distribución de sentimientos por evaluación
     */
    @Query(value = "CALL sp_get_sentiment_distribution_by_evaluation(:idEvaluacion)", nativeQuery = true)
    List<Map<String, Object>> getDistributionByEvaluation(@Param("idEvaluacion") Long idEvaluacion);

    /**
     * Obtiene alertas de alto riesgo en los últimos N días
     */
    @Query(value = "CALL sp_get_high_risk_alerts(:daysBack)", nativeQuery = true)
    List<Map<String, Object>> getHighRiskAlerts(@Param("daysBack") Integer daysBack);
}

