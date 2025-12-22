package com.example.rntn.service;

import com.example.rntn.dto.request.EvaluacionPreguntaRequest;
import com.example.rntn.dto.response.EvaluacionPreguntaResponse;
import com.example.rntn.entity.EvaluacionPregunta;
import com.example.rntn.exception.ResourceNotFoundException;
import com.example.rntn.repository.EvaluacionPreguntaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para EvaluacionPregunta
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvaluacionPreguntaService {

    private final EvaluacionPreguntaRepository preguntaRepository;

    public EvaluacionPreguntaResponse crearPregunta(EvaluacionPreguntaRequest request) {
        log.info("Creando pregunta de evaluación");

        EvaluacionPregunta pregunta = EvaluacionPregunta.builder()
            .textoEvaluacionPregunta(request.getTextoEvaluacionPregunta())
            .build();

        pregunta = preguntaRepository.save(pregunta);

        return mapToResponse(pregunta);
    }

    @Transactional(readOnly = true)
    public EvaluacionPreguntaResponse obtenerPregunta(Integer id) {
        EvaluacionPregunta pregunta = preguntaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada: " + id));

        return mapToResponse(pregunta);
    }

    @Transactional(readOnly = true)
    public Page<EvaluacionPreguntaResponse> listarPreguntas(Pageable pageable) {
        Page<EvaluacionPregunta> preguntas = preguntaRepository.findAll(pageable);
        return preguntas.map(this::mapToResponse);
    }

    public EvaluacionPreguntaResponse actualizarPregunta(Integer id, EvaluacionPreguntaRequest request) {
        log.info("Actualizando pregunta: {}", id);

        EvaluacionPregunta pregunta = preguntaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada: " + id));

        pregunta.setTextoEvaluacionPregunta(request.getTextoEvaluacionPregunta());

        pregunta = preguntaRepository.save(pregunta);

        return mapToResponse(pregunta);
    }

    public void eliminarPregunta(Integer id) {
        log.info("Eliminando pregunta: {}", id);

        EvaluacionPregunta pregunta = preguntaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada: " + id));

        preguntaRepository.delete(pregunta);
    }

    /**
     * Listar respuestas asociadas a una pregunta
     */
    @Transactional(readOnly = true)
    public List<com.example.rntn.dto.response.EvaluacionRespuestaResponse> listarRespuestasPorPregunta(Integer idPregunta) {
        EvaluacionPregunta pregunta = preguntaRepository.findById(idPregunta)
            .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada: " + idPregunta));

        return pregunta.getRespuestas().stream()
            .map(respuesta -> com.example.rntn.dto.response.EvaluacionRespuestaResponse.builder()
                .idEvaluacionRespuesta(respuesta.getIdEvaluacionRespuesta())
                .idEvaluacionPregunta(pregunta.getIdEvaluacionPregunta())
                .textoPregunta(pregunta.getTextoEvaluacionPregunta())
                .textoEvaluacionRespuesta(respuesta.getTextoEvaluacionRespuesta())
                .textoSetEvaluacionRespuesta(respuesta.getTextoSetEvaluacionRespuesta())
                .labelEvaluacionRespuesta(respuesta.getLabelEvaluacionRespuesta())
                .confidenceScore(respuesta.getConfidenceScore())
                .createdAt(respuesta.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }

    private EvaluacionPreguntaResponse mapToResponse(EvaluacionPregunta pregunta) {
        return EvaluacionPreguntaResponse.builder()
            .idEvaluacionPregunta(pregunta.getIdEvaluacionPregunta())
            .textoEvaluacionPregunta(pregunta.getTextoEvaluacionPregunta())
            .cantidadRespuestas(pregunta.getRespuestas() != null ? pregunta.getRespuestas().size() : 0)
            .createdAt(pregunta.getCreatedAt())
            .build();
    }
}
