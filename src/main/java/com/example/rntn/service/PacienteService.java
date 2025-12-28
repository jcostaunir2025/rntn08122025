package com.example.rntn.service;

import com.example.rntn.dto.request.PacienteRequest;
import com.example.rntn.dto.response.PacienteResponse;
import com.example.rntn.entity.Paciente;
import com.example.rntn.exception.ResourceNotFoundException;
import com.example.rntn.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para Pacientes
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    /**
     * Crear un nuevo paciente
     */
    public PacienteResponse crearPaciente(PacienteRequest request) {
        log.info("Creando paciente: {}", request.getDocPaciente());

        // Verificar que no exista el documento
        if (pacienteRepository.existsByDocPaciente(request.getDocPaciente())) {
            throw new IllegalArgumentException("Ya existe un paciente con el documento: " + request.getDocPaciente());
        }

        Paciente paciente = Paciente.builder()
            .docPaciente(request.getDocPaciente())
            .nombrePaciente(request.getNombrePaciente())
            .direccionPaciente(request.getDireccionPaciente())
            .emailPaciente(request.getEmailPaciente())
            .telefonoPaciente(request.getTelefonoPaciente())
            .fechaPaciente(request.getFechaPaciente())
            .generoPaciente(request.getGeneroPaciente())
            .contactoPaciente(request.getContactoPaciente())
            .telefonoContactoPaciente(request.getTelefonoContactoPaciente())
            .estatusPaciente(request.getEstatusPaciente())
            .build();

        paciente = pacienteRepository.save(paciente);

        return mapToResponse(paciente);
    }

    /**
     * Obtener paciente por ID
     */
    @Transactional(readOnly = true)
    public PacienteResponse obtenerPaciente(Integer id) {
        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + id));

        return mapToResponse(paciente);
    }

    /**
     * Listar pacientes con paginación y filtros
     */
    @Transactional(readOnly = true)
    public Page<PacienteResponse> listarPacientes(String estatus, String search, Pageable pageable) {
        Page<Paciente> pacientes;

        if (search != null && !search.isEmpty()) {
            pacientes = pacienteRepository.searchPacientes(search, pageable);
        } else if (estatus != null && !estatus.isEmpty()) {
            pacientes = pacienteRepository.findByEstatusPaciente(estatus, pageable);
        } else {
            pacientes = pacienteRepository.findAll(pageable);
        }

        return pacientes.map(this::mapToResponse);
    }

    /**
     * Actualizar paciente
     */
    public PacienteResponse actualizarPaciente(Integer id, PacienteRequest request) {
        log.info("Actualizando paciente: {}", id);

        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + id));

        // Verificar documento si cambió
        if (!paciente.getDocPaciente().equals(request.getDocPaciente())) {
            if (pacienteRepository.existsByDocPaciente(request.getDocPaciente())) {
                throw new IllegalArgumentException("Ya existe un paciente con el documento: " + request.getDocPaciente());
            }
        }

        paciente.setDocPaciente(request.getDocPaciente());
        paciente.setNombrePaciente(request.getNombrePaciente());
        paciente.setDireccionPaciente(request.getDireccionPaciente());
        paciente.setEmailPaciente(request.getEmailPaciente());
        paciente.setTelefonoPaciente(request.getTelefonoPaciente());
        paciente.setFechaPaciente(request.getFechaPaciente());
        paciente.setGeneroPaciente(request.getGeneroPaciente());
        paciente.setContactoPaciente(request.getContactoPaciente());
        paciente.setTelefonoContactoPaciente(request.getTelefonoContactoPaciente());
        paciente.setEstatusPaciente(request.getEstatusPaciente());

        paciente = pacienteRepository.save(paciente);

        return mapToResponse(paciente);
    }

    /**
     * Eliminar paciente (soft delete cambiando estatus)
     */
    public void eliminarPaciente(Integer id) {
        log.info("Eliminando paciente: {}", id);

        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + id));

        paciente.setEstatusPaciente("INACTIVO");
        pacienteRepository.save(paciente);
    }

    /**
     * Mapear entidad a DTO de respuesta
     */
    private PacienteResponse mapToResponse(Paciente paciente) {
        return PacienteResponse.builder()
            .idPaciente(paciente.getIdPaciente())
            .docPaciente(paciente.getDocPaciente())
            .nombrePaciente(paciente.getNombrePaciente())
            .direccionPaciente(paciente.getDireccionPaciente())
            .emailPaciente(paciente.getEmailPaciente())
            .telefonoPaciente(paciente.getTelefonoPaciente())
            .fechaPaciente(paciente.getFechaPaciente())
            .generoPaciente(paciente.getGeneroPaciente())
            .contactoPaciente(paciente.getContactoPaciente())
            .telefonoContactoPaciente(paciente.getTelefonoContactoPaciente())
            .estatusPaciente(paciente.getEstatusPaciente())
            .createdAt(paciente.getCreatedAt())
            .updatedAt(paciente.getUpdatedAt())
            .build();
    }
}

