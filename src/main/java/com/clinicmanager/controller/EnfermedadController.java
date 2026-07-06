package com.clinicmanager.controller;

import com.clinicmanager.model.Enfermedad;
import com.clinicmanager.model.Usuario;
import com.clinicmanager.repository.EnfermedadRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/enfermedades")
public class EnfermedadController {

    @Autowired
    private EnfermedadRepository enfermedadRepository;

    @GetMapping
    public ResponseEntity<?> getAllEnfermedades(HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        return ResponseEntity.ok(enfermedadRepository.findByActivoTrue());
    }

    @PostMapping
    public ResponseEntity<?> createEnfermedad(@RequestBody Enfermedad enfermedad, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        
        long count = enfermedadRepository.count() + 1;
        String newId = "ENF" + count;
        while (enfermedadRepository.existsById(newId)) {
            count++;
            newId = "ENF" + count;
        }
        enfermedad.setId(newId);
        enfermedad.setActivo(true);

        Enfermedad saved = enfermedadRepository.save(enfermedad);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEnfermedad(@PathVariable String id, @RequestBody Enfermedad updatedInfo, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        Optional<Enfermedad> enfOpt = enfermedadRepository.findById(id);
        if (enfOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Enfermedad enfermedad = enfOpt.get();
        enfermedad.setNombre(updatedInfo.getNombre());
        enfermedad.setDescripcion(updatedInfo.getDescripcion());
        enfermedad.setGravedad(updatedInfo.getGravedad());

        Enfermedad saved = enfermedadRepository.save(enfermedad);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEnfermedad(@PathVariable String id, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        Optional<Enfermedad> enfOpt = enfermedadRepository.findById(id);
        if (enfOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Enfermedad enfermedad = enfOpt.get();
        enfermedad.setActivo(false); 
        enfermedadRepository.save(enfermedad);

        return ResponseEntity.ok(Map.of("message", "Enfermedad desactivada con éxito"));
    }
}
