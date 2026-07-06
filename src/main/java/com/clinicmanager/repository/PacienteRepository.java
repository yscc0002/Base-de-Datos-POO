package com.clinicmanager.repository;

import com.clinicmanager.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, String> {
    Optional<Paciente> findByCedula(String cedula);
    List<Paciente> findByActivoTrue();
    boolean existsByCedula(String cedula);
}
