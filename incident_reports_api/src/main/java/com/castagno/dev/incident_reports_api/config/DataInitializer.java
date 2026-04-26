package com.castagno.dev.incident_reports_api.config;

import com.castagno.dev.incident_reports_api.model.ERole;
import com.castagno.dev.incident_reports_api.model.Role;
import com.castagno.dev.incident_reports_api.model.User;
import com.castagno.dev.incident_reports_api.repository.RoleRepository;
import com.castagno.dev.incident_reports_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Se ejecuta UNA SOLA VEZ al arrancar Spring Boot, después de que
 * Hibernate ya creó/actualizó todas las tablas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedUsers();
    }

    //  Roles

    private void seedRoles() {
        for (ERole eRole : ERole.values()) {
            if (roleRepository.findByName(eRole).isEmpty()) {
                roleRepository.save(new Role(eRole));
                log.info("Role '{}' created", eRole.name());
            }
        }
    }

    // Usuarios iniciales

    private void seedUsers() {
        seedAdmin();
        seedAnalyst();
    }

    /**
     * Admin por defecto.
     * Credenciales: admin / Admin1234!
     */
    private void seedAdmin() {
        if (userRepository.existsByUsername("admin")) return;

        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow();

        User admin = User.builder()
                .username("admin")
                .email("admin@incidentreport.com")
                .password(passwordEncoder.encode("Admin1234!"))
                .fullName("System Administrator")
                .enabled(true)
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);
        log.info("Default admin user created — username: 'admin', password: 'Admin1234!'");
    }

    /**
     * Analyst de prueba.
     * Credenciales: analyst / Analyst1234!
     */
    private void seedAnalyst() {
        if (userRepository.existsByUsername("analyst")) return;

        Role analystRole = roleRepository.findByName(ERole.ROLE_ANALYST)
                .orElseThrow();

        User analyst = User.builder()
                .username("analyst")
                .email("analyst@incidentreport.com")
                .password(passwordEncoder.encode("Analyst1234!"))
                .fullName("Default Analyst")
                .enabled(true)
                .roles(Set.of(analystRole))
                .build();

        userRepository.save(analyst);
        log.info("Default analyst user created — username: 'analyst', password: 'Analyst1234!'");
    }
}
