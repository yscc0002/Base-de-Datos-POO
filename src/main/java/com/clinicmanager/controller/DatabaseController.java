package com.clinicmanager.controller;

import com.clinicmanager.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    @Autowired
    private DataSource dataSource;

    private static final String BACKUP_DIR = "./backups";

    @GetMapping("/backups")
    public ResponseEntity<?> getBackups(HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        File directory = new File(BACKUP_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".sql"));
        List<Map<String, Object>> backupList = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                backupList.add(Map.of(
                    "filename", file.getName(),
                    "size", file.length(),
                    "lastModified", file.lastModified()
                ));
            }
        }

        return ResponseEntity.ok(backupList);
    }

    @PostMapping("/backup")
    public ResponseEntity<?> createBackup(HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        try {
            File directory = new File(BACKUP_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "backup_" + timestamp + ".sql";
            File backupFile = new File(directory, filename);

            
            String dumpCmd = "mariadb-dump";
            try {
                Process p = Runtime.getRuntime().exec(new String[]{dumpCmd, "--help"});
                p.waitFor();
            } catch (Exception ex) {
                
                dumpCmd = "C:/Program Files/MariaDB 11.4/bin/mariadb-dump.exe";
            }

            
            ProcessBuilder pb = new ProcessBuilder(dumpCmd, "-u", "root", "-pAdmin1234!", "clinicdb");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            
            try (InputStream is = process.getInputStream();
                 FileOutputStream fos = new FileOutputStream(backupFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al crear respaldo. Código de salida: " + exitCode));
            }

            return ResponseEntity.ok(Map.of(
                "message", "Respaldo SQL de MariaDB generado con éxito",
                "filename", filename
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Error al crear respaldo: " + e.getMessage()));
        }
    }

    @PostMapping("/restore")
    public ResponseEntity<?> restoreBackup(@RequestBody Map<String, String> payload, HttpSession session) {
        Usuario loggedUser = (Usuario) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRol().equalsIgnoreCase("administrador")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        String filename = payload.get("filename");
        if (filename == null || !filename.endsWith(".sql")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Archivo de respaldo inválido"));
        }

        try {
            File backupFile = new File(BACKUP_DIR, filename);
            if (!backupFile.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "El archivo de respaldo no existe"));
            }

            
            String clientCmd = "mariadb";
            try {
                Process p = Runtime.getRuntime().exec(new String[]{clientCmd, "--help"});
                p.waitFor();
            } catch (Exception ex) {
                
                clientCmd = "C:/Program Files/MariaDB 11.4/bin/mariadb.exe";
            }

            
            ProcessBuilder pb = new ProcessBuilder(clientCmd, "-u", "root", "-pAdmin1234!", "clinicdb");
            pb.redirectInput(ProcessBuilder.Redirect.from(backupFile));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            
            try (InputStream is = process.getInputStream()) {
                byte[] buffer = new byte[4096];
                while (is.read(buffer) != -1) {
                    
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al restaurar respaldo. Código de salida: " + exitCode));
            }

            return ResponseEntity.ok(Map.of("message", "Base de datos restaurada correctamente a partir de " + filename));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Error al restaurar respaldo: " + e.getMessage()));
        }
    }
}
