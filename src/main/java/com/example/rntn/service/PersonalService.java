package com.example.rntn.service;

import com.example.rntn.dto.request.PersonalRequest;
import com.example.rntn.dto.response.PersonalResponse;
import com.example.rntn.entity.Personal;
import com.example.rntn.entity.Usuario;
import com.example.rntn.exception.ResourceNotFoundException;
import com.example.rntn.repository.PersonalRepository;
import com.example.rntn.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PersonalService {

    private final PersonalRepository personalRepository;
    private final UsuarioRepository usuarioRepository;

    public PersonalResponse crearPersonal(PersonalRequest request) {
        log.info("Creando personal: {}", request.getDocPersonal());

        if (personalRepository.existsByDocPersonal(request.getDocPersonal())) {
            throw new IllegalArgumentException("Ya existe personal con el documento: " + request.getDocPersonal());
        }

        Personal personal = Personal.builder()
            .docPersonal(request.getDocPersonal())
            .nombrePersonal(request.getNombrePersonal())
            .emailPersonal(request.getEmailPersonal())
            .telefonoPersonal(request.getTelefonoPersonal())
            .estatusPersonal(request.getEstatusPersonal())
            .build();

        // Set Usuario if provided (1:1 relationship)
        if (request.getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.getIdUsuario()));
            personal.setUsuario(usuario);
        }

        personal = personalRepository.save(personal);

        return mapToResponse(personal);
    }

    @Transactional(readOnly = true)
    public PersonalResponse obtenerPersonal(Integer id) {
        Personal personal = personalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado: " + id));

        return mapToResponse(personal);
    }

    @Transactional(readOnly = true)
    public Page<PersonalResponse> listarPersonal(String estatus, Pageable pageable) {
        Page<Personal> personal;

        if (estatus != null && !estatus.isEmpty()) {
            personal = personalRepository.findByEstatusPersonal(estatus, pageable);
        } else {
            personal = personalRepository.findAll(pageable);
        }

        return personal.map(this::mapToResponse);
    }

    public PersonalResponse actualizarPersonal(Integer id, PersonalRequest request) {
        log.info("Actualizando personal: {}", id);

        Personal personal = personalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado: " + id));

        if (!personal.getDocPersonal().equals(request.getDocPersonal())) {
            if (personalRepository.existsByDocPersonal(request.getDocPersonal())) {
                throw new IllegalArgumentException("Ya existe personal con el documento: " + request.getDocPersonal());
            }
        }

        personal.setDocPersonal(request.getDocPersonal());
        personal.setNombrePersonal(request.getNombrePersonal());
        personal.setEmailPersonal(request.getEmailPersonal());
        personal.setTelefonoPersonal(request.getTelefonoPersonal());
        personal.setEstatusPersonal(request.getEstatusPersonal());

        // Update Usuario if provided (1:1 relationship)
        if (request.getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.getIdUsuario()));
            personal.setUsuario(usuario);
        } else {
            personal.setUsuario(null);
        }

        personal = personalRepository.save(personal);

        return mapToResponse(personal);
    }

    public void eliminarPersonal(Integer id) {
        log.info("Eliminando personal: {}", id);

        Personal personal = personalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado: " + id));

        personal.setEstatusPersonal("INACTIVO");
        personalRepository.save(personal);
    }

    private PersonalResponse mapToResponse(Personal personal) {
        PersonalResponse.PersonalResponseBuilder builder = PersonalResponse.builder()
            .idPersonal(personal.getIdPersonal())
            .docPersonal(personal.getDocPersonal())
            .nombrePersonal(personal.getNombrePersonal())
            .emailPersonal(personal.getEmailPersonal())
            .telefonoPersonal(personal.getTelefonoPersonal())
            .estatusPersonal(personal.getEstatusPersonal())
            .createdAt(personal.getCreatedAt())
            .updatedAt(personal.getUpdatedAt());

        // Include usuario information if 1:1 relationship exists
        if (personal.getUsuario() != null) {
            builder.idUsuario(personal.getUsuario().getIdUsuario())
                   .nombreUsuario(personal.getUsuario().getNombreUsuario());
        }

        return builder.build();
    }
}

