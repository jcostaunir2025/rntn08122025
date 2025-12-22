package com.example.rntn.repository;

import com.example.rntn.entity.EvaluacionPregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para EvaluacionPregunta
 */
@Repository
public interface EvaluacionPreguntaRepository extends JpaRepository<EvaluacionPregunta, Integer> {
    // Métodos básicos CRUD heredados de JpaRepository
}

