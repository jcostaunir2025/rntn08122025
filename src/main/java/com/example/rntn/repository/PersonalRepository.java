package com.example.rntn.repository;

import com.example.rntn.entity.Personal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Personal
 */
@Repository
public interface PersonalRepository extends JpaRepository<Personal, Integer> {

    Optional<Personal> findByDocPersonal(String docPersonal);

    Page<Personal> findByEstatusPersonal(String estatus, Pageable pageable);

    boolean existsByDocPersonal(String docPersonal);
}

