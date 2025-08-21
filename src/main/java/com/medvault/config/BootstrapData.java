package com.medvault.config;

import com.medvault.model.Role;
import com.medvault.model.User;
import com.medvault.repository.RoleRepository;
import com.medvault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class BootstrapData implements CommandLineRunner {
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;

    @Override
    public void run(String... args) {
        var adminRole = roleRepo.findByName("ADMIN").orElseGet(() -> roleRepo.save(Role.builder().name("ADMIN").build()));
        var doctorRole = roleRepo.findByName("DOCTOR").orElseGet(() -> roleRepo.save(Role.builder().name("DOCTOR").build()));
        var patientRole = roleRepo.findByName("PATIENT").orElseGet(() -> roleRepo.save(Role.builder().name("PATIENT").build()));

        if (!userRepo.existsByEmail("admin@medvault.local")) {
            var u = User.builder()
                    .name("Admin")
                    .email("admin@medvault.local")
                    .passwordHash(encoder.encode("Admin@123"))
                    .roles(Set.of(adminRole))
                    .enabled(true)
                    .build();
            userRepo.save(u);
        }
    }
}
