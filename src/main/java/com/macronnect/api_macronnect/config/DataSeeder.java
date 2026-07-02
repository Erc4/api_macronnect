package com.macronnect.api_macronnect.config;

import com.macronnect.api_macronnect.auth.Rol;
import com.macronnect.api_macronnect.auth.Usuario;
import com.macronnect.api_macronnect.auth.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-username}")
    private String adminUsername;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    public DataSeeder(UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByUsername(adminUsername)) {
            Usuario admin = new Usuario(
                    adminUsername,
                    passwordEncoder.encode(adminPassword));
            admin.addRol(Rol.ADMIN);
            admin.addRol(Rol.USER);
            usuarioRepository.save(admin);
        }
    }
}