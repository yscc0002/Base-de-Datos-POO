package com.clinicmanager.controller;

import com.clinicmanager.model.Cita;
import com.clinicmanager.model.Medico;
import com.clinicmanager.model.Usuario;
import com.clinicmanager.repository.CitaRepository;
import com.clinicmanager.repository.MedicoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @GetMapping
    public ResponseEntity<?> getAllCitas(HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        return ResponseEntity.ok(citaRepository.findByEsActivoTrue());
    }

    @PostMapping
    public ResponseEntity<?> createCita(@RequestBody Map<String, Object> payload, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }

        String nombre = (String) payload.get("nombre");
        String apellido = (String) payload.get("apellido");
        String cedula = (String) payload.get("cedula");
        String medicoId = (String) payload.get("medicoId");
        String fechaStr = (String) payload.get("fecha");

        if (nombre == null || apellido == null || cedula == null || medicoId == null || fechaStr == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Faltan campos obligatorios"));
        }

        LocalDate fecha = LocalDate.parse(fechaStr);
        Optional<Medico> medOpt = medicoRepository.findById(medicoId);
        if (medOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Médico no encontrado"));
        }

        Medico medico = medOpt.get();
        if (!medico.isActivo()) {
            return ResponseEntity.badRequest().body(Map.of("message", "El médico no está activo"));
        }

        
        long activeCitasCount = citaRepository.countByMedicoIdAndFechaAndEsActivoTrue(medicoId, fecha);
        if (activeCitasCount >= medico.getMaxCitas()) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "El médico " + medico.getNombre() + " " + medico.getApellido() + 
                           " ya tiene el límite máximo de citas asignadas (" + medico.getMaxCitas() + ") para la fecha " + fecha
            ));
        }

        
        long count = citaRepository.count() + 1;
        String newId = "CIT" + count;
        while (citaRepository.existsById(newId)) {
            count++;
            newId = "CIT" + count;
        }

        Cita cita = new Cita(newId, nombre, apellido, cedula, medico, fecha);
        Cita saved = citaRepository.save(cita);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/{id}/posponer")
    public ResponseEntity<?> postponeCita(@PathVariable String id, @RequestBody Map<String, String> payload, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }

        String nuevaFechaStr = payload.get("fecha");
        if (nuevaFechaStr == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nueva fecha es requerida"));
        }

        LocalDate nuevaFecha = LocalDate.parse(nuevaFechaStr);

        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Cita cita = citaOpt.get();
        if (!cita.isEsActivo()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No se puede posponer una cita cancelada"));
        }

        Medico medico = cita.getMedico();

        
        long activeCitasCount = citaRepository.countByMedicoIdAndFechaAndEsActivoTrue(medico.getId(), nuevaFecha);
        if (activeCitasCount >= medico.getMaxCitas()) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "El médico " + medico.getNombre() + " " + medico.getApellido() + 
                           " ya tiene el límite máximo de citas asignadas (" + medico.getMaxCitas() + ") para la fecha " + nuevaFecha
            ));
        }

        cita.setFecha(nuevaFecha);
        Cita saved = citaRepository.save(cita);

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelCita(@PathVariable String id, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }

        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Cita cita = citaOpt.get();
        cita.setEsActivo(false); 
        citaRepository.save(cita);

        return ResponseEntity.ok(Map.of("message", "Cita cancelada con éxito"));
    }

    @GetMapping("/disponibilidad-medicos")
    public ResponseEntity<?> getMedicosDisponibles(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }

        
        List<Medico> allMedicos = medicoRepository.findByActivoTrue();
        List<Medico> medicosDisponibles = allMedicos.stream().filter(m -> {
            long count = citaRepository.countByMedicoIdAndFechaAndEsActivoTrue(m.getId(), fecha);
            return count < m.getMaxCitas();
        }).toList();

        return ResponseEntity.ok(medicosDisponibles);
    }
}
