package com.example.rntn.repository;

import com.example.rntn.entity.ConsultaEstatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultaEstatusRepository extends JpaRepository<ConsultaEstatus, Integer> {

    Optional<ConsultaEstatus> findByNombreConsultaEstatus(String nombre);
}

