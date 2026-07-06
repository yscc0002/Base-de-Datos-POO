package com.clinicmanager.repository;

import com.clinicmanager.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, String> {
    Optional<Medico> findByCedula(String cedula);
    List<Medico> findByActivoTrue();
    boolean existsByCedula(String cedula);
}
