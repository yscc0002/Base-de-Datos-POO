package com.clinicmanager.controller;

import com.clinicmanager.model.Consulta;
import com.clinicmanager.model.Enfermedad;
import com.clinicmanager.model.Medico;
import com.clinicmanager.model.Paciente;
import com.clinicmanager.model.Usuario;
import com.clinicmanager.repository.ConsultaRepository;
import com.clinicmanager.repository.EnfermedadRepository;
import com.clinicmanager.repository.MedicoRepository;
import com.clinicmanager.repository.PacienteRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EnfermedadRepository enfermedadRepository;

    @GetMapping
    public ResponseEntity<?> getAllConsultas(HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        return ResponseEntity.ok(consultaRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getConsultaById(@PathVariable String id, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        Optional<Consulta> consOpt = consultaRepository.findById(id);
        return consOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createConsulta(@RequestBody Map<String, Object> payload, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }

        String pacienteId = (String) payload.get("pacienteId");
        String medicoId = (String) payload.get("medicoId");
        String sintomas = (String) payload.get("sintomas");
        String diagnostico = (String) payload.get("diagnostico");
        String enfermedadId = (String) payload.get("enfermedadId");
        Boolean esImportante = (Boolean) payload.get("esImportante");

        if (pacienteId == null || medicoId == null || sintomas == null || diagnostico == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Faltan campos obligatorios"));
        }

        Optional<Paciente> pacOpt = pacienteRepository.findById(pacienteId);
        if (pacOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Paciente no encontrado"));
        }

        Optional<Medico> medOpt = medicoRepository.findById(medicoId);
        if (medOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Médico no encontrado"));
        }

        Enfermedad enfermedad = null;
        if (enfermedadId != null && !enfermedadId.trim().isEmpty()) {
            Optional<Enfermedad> enfOpt = enfermedadRepository.findById(enfermedadId);
            if (enfOpt.isPresent()) {
                enfermedad = enfOpt.get();
            }
        }

        
        long count = consultaRepository.count() + 1;
        String newId = "CON" + count;
        while (consultaRepository.existsById(newId)) {
            count++;
            newId = "CON" + count;
        }

        Consulta consulta = new Consulta(
            newId, 
            pacOpt.get(), 
            medOpt.get(), 
            sintomas, 
            diagnostico, 
            enfermedad, 
            esImportante != null && esImportante
        );

        Consulta saved = consultaRepository.save(consulta);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<?> getConsultasByPaciente(@PathVariable String pacienteId, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        return ResponseEntity.ok(consultaRepository.findByPacienteId(pacienteId));
    }

    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<?> getConsultasByMedico(@PathVariable String medicoId, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        return ResponseEntity.ok(consultaRepository.findByMedicoId(medicoId));
    }
}
