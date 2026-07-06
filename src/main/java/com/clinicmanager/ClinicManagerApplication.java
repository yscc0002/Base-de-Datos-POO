package com.clinicmanager;

import com.clinicmanager.model.Usuario;
import com.clinicmanager.repository.UsuarioRepository;
import com.clinicmanager.util.SecurityUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ClinicManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicManagerApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository usuarioRepository) {
        return args -> {
            
            if (!usuarioRepository.existsById("Administrador")) {
                Usuario admin = new Usuario("Administrador", SecurityUtils.md5("123456"), "administrador", "0");
                usuarioRepository.save(admin);
                System.out.println("Default Administrator user created (Username: Administrador, Password: 123456).");
            }

            
            if (!usuarioRepository.existsById("Secretaria")) {
                Usuario sec = new Usuario("Secretaria", SecurityUtils.md5("123456"), "secretaria", "1");
                usuarioRepository.save(sec);
                System.out.println("Default Secretary user created (Username: Secretaria, Password: 123456).");
            }
        };
    }
}
