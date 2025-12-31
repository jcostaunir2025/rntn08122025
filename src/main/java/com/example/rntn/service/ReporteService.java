package com.example.rntn.service;

import com.example.rntn.dto.request.ReporteRequest;
import com.example.rntn.dto.response.ReporteResponse;
import com.example.rntn.entity.Evaluacion;
import com.example.rntn.entity.Reporte;
import com.example.rntn.entity.Usuario;
import com.example.rntn.exception.ResourceNotFoundException;
import com.example.rntn.repository.EvaluacionRepository;
import com.example.rntn.repository.ReporteRepository;
import com.example.rntn.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio de negocio para Reportes
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EvaluacionRepository evaluacionRepository;

    public ReporteResponse generarReporte(ReporteRequest request) {
        log.info("Generando reporte: {} por usuario: {}",
            request.getNombreReporte(), request.getIdUsuario());

        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.getIdUsuario()));

        Evaluacion evaluacion = evaluacionRepository.findById(request.getIdEvaluacion())
            .orElseThrow(() -> new ResourceNotFoundException("Evaluación no encontrada: " + request.getIdEvaluacion()));

        Reporte reporte = Reporte.builder()
            .usuario(usuario)
            .evaluacion(evaluacion)
            .fechageneracionReporte(LocalDateTime.now())
            .nombreReporte(request.getNombreReporte())
            .resultadoReporte(request.getResultadoReporte())
            .build();

        reporte = reporteRepository.save(reporte);

        return mapToResponse(reporte);
    }

    @Transactional(readOnly = true)
    public ReporteResponse obtenerReporte(Integer id) {
        Reporte reporte = reporteRepository.findByIdWithFullDetails(id);

        if (reporte == null) {
            throw new ResourceNotFoundException("Reporte no encontrado: " + id);
        }

        return mapToResponse(reporte);
    }

    @Transactional(readOnly = true)
    public Page<ReporteResponse> listarReportesPorUsuario(Integer idUsuario, Pageable pageable) {
        Page<Reporte> reportes = reporteRepository.findByUsuarioIdUsuario(idUsuario, pageable);
        return reportes.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReporteResponse> listarReportesPorEvaluacion(Integer idEvaluacion, Pageable pageable) {
        Page<Reporte> reportes = reporteRepository.findByEvaluacionIdEvaluacion(idEvaluacion, pageable);
        return reportes.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReporteResponse> listarReportes(Pageable pageable) {
        Page<Reporte> reportes = reporteRepository.findAll(pageable);
        return reportes.map(this::mapToResponse);
    }

    public ReporteResponse actualizarReporte(Integer id, ReporteRequest request) {
        log.info("Actualizando reporte: {}", id);

        Reporte reporte = reporteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado: " + id));

        reporte.setNombreReporte(request.getNombreReporte());
        reporte.setResultadoReporte(request.getResultadoReporte());

        reporte = reporteRepository.save(reporte);

        return mapToResponse(reporte);
    }

    public void eliminarReporte(Integer id) {
        log.info("Eliminando reporte: {}", id);

        Reporte reporte = reporteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado: " + id));

        reporteRepository.delete(reporte);
    }

    private ReporteResponse mapToResponse(Reporte reporte) {
        return ReporteResponse.builder()
            .idReporte(reporte.getIdReporte())
            .usuario(ReporteResponse.UsuarioInfo.builder()
                .idUsuario(reporte.getUsuario().getIdUsuario())
                .nombreUsuario(reporte.getUsuario().getNombreUsuario())
                .build())
            .evaluacion(ReporteResponse.EvaluacionInfo.builder()
                .idEvaluacion(reporte.getEvaluacion().getIdEvaluacion())
                .nombreEvaluacion(reporte.getEvaluacion().getNombreEvaluacion())
                .tituloEvaluacion(reporte.getEvaluacion().getTituloEvaluacion())
                .build())
            .fechageneracionReporte(reporte.getFechageneracionReporte())
            .nombreReporte(reporte.getNombreReporte())
            .resultadoReporte(reporte.getResultadoReporte())
            .createdAt(reporte.getCreatedAt())
            .updatedAt(reporte.getUpdatedAt())
            .build();
    }
}

