package com.macronnect.api_macronnect.config;

import com.macronnect.api_macronnect.auth.Rol;
import com.macronnect.api_macronnect.auth.Usuario;
import com.macronnect.api_macronnect.auth.UsuarioRepository;
import com.macronnect.api_macronnect.venta.FolioSequence;
import com.macronnect.api_macronnect.venta.FolioSequenceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final String SECUENCIA_FOLIO = "venta_folio";
    private static final long FOLIO_INICIAL = 1000L;

    private final UsuarioRepository usuarioRepository;
    private final FolioSequenceRepository folioSequenceRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-username}")
    private String adminUsername;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    public DataSeeder(UsuarioRepository usuarioRepository,
        FolioSequenceRepository folioSequenceRepository,
        PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.folioSequenceRepository = folioSequenceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Usuario admin
        if (!usuarioRepository.existsByUsername(adminUsername)) {
            Usuario admin = new Usuario(
                    adminUsername,
                    passwordEncoder.encode(adminPassword));
            admin.addRol(Rol.ADMIN);
            admin.addRol(Rol.USER);
            usuarioRepository.save(admin);
        }

        // Secuencia de folios (arranca en 1000; el primer folio será 1001)
        if (!folioSequenceRepository.existsById(SECUENCIA_FOLIO)) {
            folioSequenceRepository.save(new FolioSequence(SECUENCIA_FOLIO, FOLIO_INICIAL));
        }
    }
}