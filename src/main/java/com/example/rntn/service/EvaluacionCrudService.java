package com.example.rntn.service;

import com.example.rntn.dto.request.EvaluacionRequest;
import com.example.rntn.dto.response.EvaluacionResponse;
import com.example.rntn.entity.Evaluacion;
import com.example.rntn.exception.ResourceNotFoundException;
import com.example.rntn.repository.EvaluacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio CRUD para Evaluaciones
 * ⭐ UPDATED: Removed consulta dependency - Relationship is now in Consulta entity (N:1)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvaluacionCrudService {

    private final EvaluacionRepository evaluacionRepository;

    public EvaluacionResponse crearEvaluacion(EvaluacionRequest request) {
        log.info("Creando evaluación: {}", request.getNombreEvaluacion());

        Evaluacion evaluacion = Evaluacion.builder()
            .tituloEvaluacion(request.getTituloEvaluacion())
            .nombreEvaluacion(request.getNombreEvaluacion())
            .areaEvaluacion(request.getAreaEvaluacion())
            .build();

        evaluacion = evaluacionRepository.save(evaluacion);

        return mapToResponse(evaluacion);
    }

    @Transactional(readOnly = true)
    public EvaluacionResponse obtenerEvaluacion(Integer id) {
        Evaluacion evaluacion = evaluacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Evaluación no encontrada: " + id));

        return mapToResponse(evaluacion);
    }

    public EvaluacionResponse actualizarEvaluacion(Integer id, EvaluacionRequest request) {
        log.info("Actualizando evaluación: {}", id);

        Evaluacion evaluacion = evaluacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Evaluación no encontrada: " + id));

        evaluacion.setTituloEvaluacion(request.getTituloEvaluacion());
        evaluacion.setNombreEvaluacion(request.getNombreEvaluacion());
        evaluacion.setAreaEvaluacion(request.getAreaEvaluacion());

        evaluacion = evaluacionRepository.save(evaluacion);

        return mapToResponse(evaluacion);
    }

    public void eliminarEvaluacion(Integer id) {
        log.info("Eliminando evaluación: {}", id);

        Evaluacion evaluacion = evaluacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Evaluación no encontrada: " + id));

        evaluacionRepository.delete(evaluacion);
    }

    private EvaluacionResponse mapToResponse(Evaluacion evaluacion) {
        return EvaluacionResponse.builder()
            .idEvaluacion(evaluacion.getIdEvaluacion())
            .tituloEvaluacion(evaluacion.getTituloEvaluacion())
            .nombreEvaluacion(evaluacion.getNombreEvaluacion())
            .fechaEvaluacion(evaluacion.getFechaEvaluacion())
            .areaEvaluacion(evaluacion.getAreaEvaluacion())
            .createdAt(evaluacion.getCreatedAt())
            .updatedAt(evaluacion.getUpdatedAt())
            .build();
    }
}

