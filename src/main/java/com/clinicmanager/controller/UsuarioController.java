package com.clinicmanager.controller;

import com.clinicmanager.model.Usuario;
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
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Usuario y clave son requeridos"));
        }

        Optional<Usuario> userOpt = usuarioRepository.findById(username);
        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            if (!user.getEsActivo()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Usuario inactivo"));
            }
            String hashedInput = SecurityUtils.md5(password);
            if (user.getClave().equals(hashedInput)) {
                session.setAttribute("loggedUser", user);
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenciales incorrectas"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No autenticado"));
        }
        return ResponseEntity.ok(loggedUser);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody Usuario nuevoUsuario, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        if (usuarioRepository.existsById(nuevoUsuario.getNombreUsuario())) {
            return ResponseEntity.badRequest().body(Map.of("message", "El nombre de usuario ya existe"));
        }

        
        nuevoUsuario.setClave(SecurityUtils.md5(nuevoUsuario.getClave()));
        nuevoUsuario.setEsActivo(true);
        Usuario savedUser = usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        if (username.equalsIgnoreCase("Administrador")) {
            return ResponseEntity.badRequest().body(Map.of("message", "No se puede eliminar el administrador principal"));
        }

        if (!usuarioRepository.existsById(username)) {
            return ResponseEntity.notFound().build();
        }

        usuarioRepository.deleteById(username);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        String username = payload.get("username");
        String newPassword = payload.get("newPassword");

        if (username == null || newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Parámetros inválidos"));
        }

        Optional<Usuario> userOpt = usuarioRepository.findById(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario user = userOpt.get();
        user.setClave(SecurityUtils.md5(newPassword));
        usuarioRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Contraseña restablecida con éxito"));
    }
}
