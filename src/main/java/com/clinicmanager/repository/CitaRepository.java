package com.clinicmanager.repository;

import com.clinicmanager.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, String> {
    List<Cita> findByEsActivoTrue();
    List<Cita> findByMedicoIdAndFechaAndEsActivoTrue(String medicoId, LocalDate fecha);
    long countByMedicoIdAndFechaAndEsActivoTrue(String medicoId, LocalDate fecha);
    List<Cita> findByCedulaAndEsActivoTrue(String cedula);
}
