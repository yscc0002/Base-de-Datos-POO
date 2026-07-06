package com.clinicmanager.repository;

import com.clinicmanager.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, String> {
    List<Consulta> findByPacienteId(String pacienteId);
    List<Consulta> findByMedicoId(String medicoId);
}
