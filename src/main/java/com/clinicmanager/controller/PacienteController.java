package com.clinicmanager.controller;

import com.clinicmanager.model.Paciente;
import com.clinicmanager.model.Usuario;
import com.clinicmanager.model.Vacuna;
import com.clinicmanager.repository.PacienteRepository;
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
@RequestMapping("/api/pacientes")
public class PacienteController {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private VacunaRepository vacunaRepository;

    @GetMapping
    public ResponseEntity<?> getAllPacientes(HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        return ResponseEntity.ok(pacienteRepository.findByActivoTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPacienteById(@PathVariable String id, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        Optional<Paciente> pacOpt = pacienteRepository.findById(id);
        return pacOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/cedula/{cedula}")
    public ResponseEntity<?> getPacienteByCedula(@PathVariable String cedula, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        Optional<Paciente> pacOpt = pacienteRepository.findByCedula(cedula);
        return pacOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createPaciente(@RequestBody Paciente paciente, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || (!loggedUser.getRol().equalsIgnoreCase("administrador") && !loggedUser.getRol().equalsIgnoreCase("secretaria"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        if (pacienteRepository.existsByCedula(paciente.getCedula())) {
            return ResponseEntity.badRequest().body(Map.of("message", "La cédula ya está registrada para otro paciente"));
        }

        
        long count = pacienteRepository.count() + 1;
        String newId = "PAC" + count;
        while (pacienteRepository.existsById(newId)) {
            count++;
            newId = "PAC" + count;
        }
        paciente.setId(newId);
        paciente.setActivo(true);

        Paciente saved = pacienteRepository.save(paciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePaciente(@PathVariable String id, @RequestBody Paciente updatedInfo, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || (!loggedUser.getRol().equalsIgnoreCase("administrador") && !loggedUser.getRol().equalsIgnoreCase("secretaria"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        Optional<Paciente> pacOpt = pacienteRepository.findById(id);
        if (pacOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Paciente paciente = pacOpt.get();
        
        
        if (!paciente.getCedula().equals(updatedInfo.getCedula()) && pacienteRepository.existsByCedula(updatedInfo.getCedula())) {
            return ResponseEntity.badRequest().body(Map.of("message", "La cédula ya está registrada para otro paciente"));
        }

        paciente.setCedula(updatedInfo.getCedula());
        paciente.setNombre(updatedInfo.getNombre());
        paciente.setApellido(updatedInfo.getApellido());
        paciente.setEdad(updatedInfo.getEdad());
        paciente.setSexo(updatedInfo.getSexo());
        paciente.setPeso(updatedInfo.getPeso());
        paciente.setEstatura(updatedInfo.getEstatura());
        paciente.setTipoSangre(updatedInfo.getTipoSangre());
        paciente.setDireccion(updatedInfo.getDireccion());
        paciente.setTelefono(updatedInfo.getTelefono());

        Paciente saved = pacienteRepository.save(paciente);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePaciente(@PathVariable String id, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || (!loggedUser.getRol().equalsIgnoreCase("administrador") && !loggedUser.getRol().equalsIgnoreCase("secretaria"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        Optional<Paciente> pacOpt = pacienteRepository.findById(id);
        if (pacOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Paciente paciente = pacOpt.get();
        paciente.setActivo(false); 
        pacienteRepository.save(paciente);

        return ResponseEntity.ok(Map.of("message", "Paciente desactivado correctamente"));
    }

    @PostMapping("/{id}/vacunas")
    public ResponseEntity<?> applyVaccine(@PathVariable String id, @RequestBody Map<String, String> payload, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }

        String vacunaId = payload.get("vacunaId");
        if (vacunaId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "ID de vacuna es requerido"));
        }

        Optional<Paciente> pacOpt = pacienteRepository.findById(id);
        if (pacOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Paciente no encontrado"));
        }

        Optional<Vacuna> vacOpt = vacunaRepository.findById(vacunaId);
        if (vacOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Vacuna no encontrada"));
        }

        Paciente paciente = pacOpt.get();
        Vacuna vacuna = vacOpt.get();

        
        if (paciente.getVacunas().stream().anyMatch(v -> v.getId().equals(vacunaId))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Esta vacuna ya fue aplicada a este paciente anteriormente"));
        }

        paciente.agregarVacuna(vacuna);
        pacienteRepository.save(paciente);

        return ResponseEntity.ok(Map.of("message", "Vacuna aplicada con éxito", "paciente", paciente));
    }
}
