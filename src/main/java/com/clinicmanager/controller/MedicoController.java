package com.clinicmanager.controller;

import com.clinicmanager.model.Medico;
import com.clinicmanager.model.Usuario;
import com.clinicmanager.repository.MedicoRepository;
import com.clinicmanager.repository.UsuarioRepository;
import com.clinicmanager.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<?> getAllMedicos(HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        
        return ResponseEntity.ok(medicoRepository.findByActivoTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMedicoById(@PathVariable String id, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        Optional<Medico> medOpt = medicoRepository.findById(id);
        return medOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMedico(@RequestBody Medico medico, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        if (medicoRepository.existsByCedula(medico.getCedula())) {
            return ResponseEntity.badRequest().body(Map.of("message", "La cédula ya está registrada para otro médico"));
        }

        
        long count = medicoRepository.count() + 1;
        String newId = "MED" + count;
        while (medicoRepository.existsById(newId)) {
            count++;
            newId = "MED" + count;
        }
        medico.setId(newId);
        medico.setActivo(true);

        Medico savedMedico = medicoRepository.save(medico);

        
        String defaultUsername = (medico.getNombre().substring(0, 1) + medico.getApellido()).replaceAll("\\s+", "").toLowerCase() + count;
        String defaultPasswordHash = SecurityUtils.md5("123456"); 
        Usuario doctorUser = new Usuario(defaultUsername, defaultPasswordHash, "medico", savedMedico.getId());
        usuarioRepository.save(doctorUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "medico", savedMedico,
            "usernameCreado", defaultUsername,
            "claveTemporal", "123456"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMedico(@PathVariable String id, @RequestBody Medico updatedInfo, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        Optional<Medico> medOpt = medicoRepository.findById(id);
        if (medOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Medico medico = medOpt.get();
        
        
        if (!medico.getCedula().equals(updatedInfo.getCedula()) && medicoRepository.existsByCedula(updatedInfo.getCedula())) {
            return ResponseEntity.badRequest().body(Map.of("message", "La cédula ya está registrada por otro médico"));
        }

        medico.setCedula(updatedInfo.getCedula());
        medico.setNombre(updatedInfo.getNombre());
        medico.setApellido(updatedInfo.getApellido());
        medico.setEdad(updatedInfo.getEdad());
        medico.setSexo(updatedInfo.getSexo());
        medico.setEspecialidad(updatedInfo.getEspecialidad());
        medico.setMaxCitas(updatedInfo.getMaxCitas());

        Medico saved = medicoRepository.save(medico);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedico(@PathVariable String id, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        Optional<Medico> medOpt = medicoRepository.findById(id);
        if (medOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Medico medico = medOpt.get();
        medico.setActivo(false); 
        medicoRepository.save(medico);

        
        usuarioRepository.deleteByLinkId(id);

        return ResponseEntity.ok(Map.of("message", "Médico e inicio de sesión desactivados correctamente"));
    }
}
