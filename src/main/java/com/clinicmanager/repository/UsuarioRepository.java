package com.clinicmanager.repository;

import com.clinicmanager.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByNombreUsuarioAndEsActivoTrue(String nombreUsuario);
    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);
    void deleteByLinkId(String linkId);
}
