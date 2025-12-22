package com.example.rntn.service;

import com.example.rntn.dto.request.ConsultaRequest;
import com.example.rntn.dto.response.ConsultaResponse;
import com.example.rntn.entity.Consulta;
import com.example.rntn.entity.ConsultaEstatus;
import com.example.rntn.entity.Paciente;
import com.example.rntn.entity.Personal;
import com.example.rntn.exception.ResourceNotFoundException;
import com.example.rntn.repository.ConsultaRepository;
import com.example.rntn.repository.PacienteRepository;
import com.example.rntn.repository.PersonalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final PersonalRepository personalRepository;

    public ConsultaResponse crearConsulta(ConsultaRequest request) {
        log.info("Creando consulta para paciente: {}, personal: {}",
            request.getIdPaciente(), request.getIdPersonal());

        Paciente paciente = pacienteRepository.findById(request.getIdPaciente())
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + request.getIdPaciente()));

        Personal personal = personalRepository.findById(request.getIdPersonal())
            .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado: " + request.getIdPersonal()));

        Consulta consulta = Consulta.builder()
            .paciente(paciente)
            .personal(personal)
            .fechahoraConsulta(request.getFechahoraConsulta())
            .estatusConsulta(request.getEstatusConsulta())
            .build();

        consulta = consultaRepository.save(consulta);

        return mapToResponse(consulta);
    }

    @Transactional(readOnly = true)
    public ConsultaResponse obtenerConsulta(Integer id) {
        Consulta consulta = consultaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Consulta no encontrada: " + id));

        return mapToResponse(consulta);
    }

    @Transactional(readOnly = true)
    public Page<ConsultaResponse> listarConsultasPorPaciente(
            Integer idPaciente, LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {

        Page<Consulta> consultas;

        if (desde != null && hasta != null) {
            consultas = consultaRepository.findByPacienteAndDateRange(idPaciente, desde, hasta, pageable);
        } else {
            consultas = consultaRepository.findByPacienteIdPaciente(idPaciente, pageable);
        }

        return consultas.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ConsultaResponse> listarConsultasPorPersonal(Integer idPersonal, Pageable pageable) {
        Page<Consulta> consultas = consultaRepository.findByPersonalIdPersonal(idPersonal, pageable);
        return consultas.map(this::mapToResponse);
    }

    public ConsultaResponse actualizarEstadoConsulta(Integer id, Integer nuevoEstatusId) {
        log.info("Actualizando estado de consulta {} a {}", id, nuevoEstatusId);

        Consulta consulta = consultaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Consulta no encontrada: " + id));

        consulta.setEstatusConsulta(nuevoEstatusId);

        if (ConsultaEstatus.COMPLETADA.equals(nuevoEstatusId) && consulta.getFechafinConsulta() == null) {
            consulta.setFechafinConsulta(LocalDateTime.now());
        }

        consulta = consultaRepository.save(consulta);

        return mapToResponse(consulta);
    }

    public ConsultaResponse finalizarConsulta(Integer id, LocalDateTime fechaFin) {
        log.info("Finalizando consulta: {}", id);

        Consulta consulta = consultaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Consulta no encontrada: " + id));

        consulta.setEstatusConsulta(ConsultaEstatus.COMPLETADA);
        consulta.setFechafinConsulta(fechaFin != null ? fechaFin : LocalDateTime.now());

        consulta = consultaRepository.save(consulta);

        return mapToResponse(consulta);
    }

    private ConsultaResponse mapToResponse(Consulta consulta) {
        return ConsultaResponse.builder()
            .idConsulta(consulta.getIdConsulta())
            .paciente(ConsultaResponse.PacienteBasicInfo.builder()
                .idPaciente(consulta.getPaciente().getIdPaciente())
                .nombrePaciente(consulta.getPaciente().getNombrePaciente())
                .docPaciente(consulta.getPaciente().getDocPaciente())
                .build())
            .personal(ConsultaResponse.PersonalBasicInfo.builder()
                .idPersonal(consulta.getPersonal().getIdPersonal())
                .nombrePersonal(consulta.getPersonal().getNombrePersonal())
                .docPersonal(consulta.getPersonal().getDocPersonal())
                .build())
            .fechahoraConsulta(consulta.getFechahoraConsulta())
            .fechafinConsulta(consulta.getFechafinConsulta())
            .estatusConsulta(consulta.getEstatusConsulta())
            .estatusConsultaNombre(consulta.getConsultaEstatus() != null ?
                consulta.getConsultaEstatus().getNombreConsultaEstatus() : null)
            .createdAt(consulta.getCreatedAt())
            .updatedAt(consulta.getUpdatedAt())
            .build();
    }
}

