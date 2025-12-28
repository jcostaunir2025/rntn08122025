package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA para Paciente
 * Representa un paciente en el sistema de análisis de sentimientos
 */
@Entity
@Table(name = "paciente", indexes = {
    @Index(name = "idx_doc_paciente", columnList = "doc_paciente"),
    @Index(name = "idx_estatus_paciente", columnList = "estatus_paciente")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Integer idPaciente;

    @Column(name = "doc_paciente", unique = true, nullable = false, length = 20)
    private String docPaciente;

    @Column(name = "nombre_paciente", nullable = false, length = 100)
    private String nombrePaciente;

    @Column(name = "direccion_paciente", length = 255)
    private String direccionPaciente;

    @Column(name = "email_paciente", length = 100)
    private String emailPaciente;

    @Column(name = "telefono_paciente", length = 20)
    private String telefonoPaciente;

    @Column(name = "fecha_paciente")
    private LocalDate fechaPaciente;

    @Column(name = "genero_paciente", length = 20)
    private String generoPaciente;

    @Column(name = "contacto_paciente", length = 100)
    private String contactoPaciente;

    @Column(name = "telefono_contacto_paciente", length = 20)
    private String telefonoContactoPaciente;

    @Column(name = "estatus_paciente", length = 20)
    @Builder.Default
    private String estatusPaciente = "ACTIVO";

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Consulta> consultas = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void addConsulta(Consulta consulta) {
        consultas.add(consulta);
        consulta.setPaciente(this);
    }

    public void removeConsulta(Consulta consulta) {
        consultas.remove(consulta);
        consulta.setPaciente(null);
    }
}

