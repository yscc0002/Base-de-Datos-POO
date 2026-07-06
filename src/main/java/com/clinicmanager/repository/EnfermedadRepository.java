package com.clinicmanager.repository;

import com.clinicmanager.model.Enfermedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnfermedadRepository extends JpaRepository<Enfermedad, String> {
    List<Enfermedad> findByActivoTrue();
}
