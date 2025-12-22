package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "personal", indexes = {
    @Index(name = "idx_doc_personal", columnList = "doc_personal"),
    @Index(name = "idx_email_personal", columnList = "email_personal")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Personal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_personal")
    private Integer idPersonal;

    @Column(name = "doc_personal", unique = true, nullable = false, length = 20)
    private String docPersonal;

    @Column(name = "nombre_personal", nullable = false, length = 100)
    private String nombrePersonal;

    @Column(name = "email_personal", length = 100)
    private String emailPersonal;

    @Column(name = "telefono_personal", length = 20)
    private String telefonoPersonal;

    @Column(name = "estatus_personal", length = 20)
    @Builder.Default
    private String estatusPersonal = "ACTIVO";

    // ⭐ 1:1 relationship with Usuario
    @OneToOne
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "personal", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Consulta> consultas = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

