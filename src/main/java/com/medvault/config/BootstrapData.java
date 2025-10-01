package com.medvault.config;

import com.medvault.model.Role;
import com.medvault.model.User;
import com.medvault.model.DoctorPatient;
import com.medvault.repository.RoleRepository;
import com.medvault.repository.UserRepository;
import com.medvault.repository.DoctorPatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BootstrapData implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DoctorPatientRepository doctorPatientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // ---- Roles ----
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));
        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_DOCTOR")));
        Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_PATIENT")));

        // ---- Users ----
        User adminUser = userRepository.findByEmail("admin@medvault.com").orElseGet(() -> {
            User u = User.builder()
                    .email("admin@medvault.com")
                    .name("admin")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .build();
            u.getRoles().add(adminRole);
            return userRepository.save(u);
        });

        User doctorUser = userRepository.findByEmail("doctor1@medvault.com").orElseGet(() -> {
            User u = User.builder()
                    .email("doctor1@medvault.com")
                    .name("doctor1")
                    .passwordHash(passwordEncoder.encode("doctor123"))
                    .build();
            u.getRoles().add(doctorRole);
            return userRepository.save(u);
        });

        User patientUser = userRepository.findByEmail("patient1@medvault.com").orElseGet(() -> {
            User u = User.builder()
                    .email("patient1@medvault.com")
                    .name("patient1")
                    .passwordHash(passwordEncoder.encode("patient123"))
                    .build();
            u.getRoles().add(patientRole);
            return userRepository.save(u);
        });

        // ---- Doctor–Patient Assignment ----
        if (!doctorPatientRepository.existsByDoctorAndPatient(doctorUser, patientUser)) {
            doctorPatientRepository.save(
                    DoctorPatient.builder()
                            .doctor(doctorUser)
                            .patient(patientUser)
                            .build()
            );
        }
    }
}
