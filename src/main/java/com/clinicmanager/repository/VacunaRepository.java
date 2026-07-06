package com.clinicmanager.repository;

import com.clinicmanager.model.Vacuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacunaRepository extends JpaRepository<Vacuna, String> {
    List<Vacuna> findByActivoTrue();
}
