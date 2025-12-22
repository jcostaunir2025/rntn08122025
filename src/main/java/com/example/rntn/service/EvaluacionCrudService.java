package com.example.rntn.service;

import com.example.rntn.dto.request.EvaluacionRequest;
import com.example.rntn.dto.response.EvaluacionResponse;
import com.example.rntn.entity.Consulta;
import com.example.rntn.entity.Evaluacion;
import com.example.rntn.exception.ResourceNotFoundException;
import com.example.rntn.repository.ConsultaRepository;
import com.example.rntn.repository.EvaluacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio CRUD para Evaluaciones
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvaluacionCrudService {

    private final EvaluacionRepository evaluacionRepository;
    private final ConsultaRepository consultaRepository;

    public EvaluacionResponse crearEvaluacion(EvaluacionRequest request) {
        log.info("Creando evaluación: {}", request.getNombreEvaluacion());

        Consulta consulta = consultaRepository.findById(request.getIdConsulta())
            .orElseThrow(() -> new ResourceNotFoundException("Consulta no encontrada: " + request.getIdConsulta()));

        Evaluacion evaluacion = Evaluacion.builder()
            .consulta(consulta)
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

        // Actualizar consulta si cambió
        if (!evaluacion.getConsulta().getIdConsulta().equals(request.getIdConsulta())) {
            Consulta consulta = consultaRepository.findById(request.getIdConsulta())
                .orElseThrow(() -> new ResourceNotFoundException("Consulta no encontrada: " + request.getIdConsulta()));
            evaluacion.setConsulta(consulta);
        }

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
            .idConsulta(evaluacion.getConsulta().getIdConsulta())
            .nombreEvaluacion(evaluacion.getNombreEvaluacion())
            .areaEvaluacion(evaluacion.getAreaEvaluacion())
            .paciente(EvaluacionResponse.PacienteInfo.builder()
                .idPaciente(evaluacion.getConsulta().getPaciente().getIdPaciente())
                .nombrePaciente(evaluacion.getConsulta().getPaciente().getNombrePaciente())
                .build())
            .cantidadRespuestas(0) // TODO: Contar respuestas si es necesario
            .createdAt(evaluacion.getCreatedAt())
            .updatedAt(evaluacion.getUpdatedAt())
            .build();
    }
}

