package com.clinicmanager.controller;

import com.clinicmanager.model.Usuario;
import com.clinicmanager.model.Vacuna;
import com.clinicmanager.repository.VacunaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vacunas")
public class VacunaController {

    @Autowired
    private VacunaRepository vacunaRepository;

    @GetMapping
    public ResponseEntity<?> getAllVacunas(HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        return ResponseEntity.ok(vacunaRepository.findByActivoTrue());
    }

    @PostMapping
    public ResponseEntity<?> createVacuna(@RequestBody Vacuna vacuna, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        
        long count = vacunaRepository.count() + 1;
        String newId = "VAC" + count;
        while (vacunaRepository.existsById(newId)) {
            count++;
            newId = "VAC" + count;
        }
        vacuna.setId(newId);
        vacuna.setActivo(true);

        Vacuna saved = vacunaRepository.save(vacuna);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVacuna(@PathVariable String id, @RequestBody Vacuna updatedInfo, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        Optional<Vacuna> vacOpt = vacunaRepository.findById(id);
        if (vacOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Vacuna vacuna = vacOpt.get();
        vacuna.setNombre(updatedInfo.getNombre());
        vacuna.setFabricante(updatedInfo.getFabricante());
        vacuna.setDosis(updatedInfo.getDosis());
        vacuna.setDescripcion(updatedInfo.getDescripcion());

        Vacuna saved = vacunaRepository.save(vacuna);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVacuna(@PathVariable String id, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        Optional<Vacuna> vacOpt = vacunaRepository.findById(id);
        if (vacOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Vacuna vacuna = vacOpt.get();
        vacuna.setActivo(false); 
        vacunaRepository.save(vacuna);

        return ResponseEntity.ok(Map.of("message", "Vacuna desactivada con éxito"));
    }
}
