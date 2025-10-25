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
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BootstrapData implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DoctorPatientRepository doctorPatientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("✅ Bootstrapping MedVault data...");

        // ---- 1. Ensure Roles Exist ----
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_DOCTOR")));

        Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_PATIENT")));

        // ---- 2. Ensure Users Exist ----
        User adminUser = createUserIfNotExists(
                "admin@medvault.com",
                "Admin",
                "admin123",
                adminRole
        );

        User doctorUser = createUserIfNotExists(
                "doctor1@medvault.com",
                "Doctor One",
                "doctor123",
                doctorRole
        );

        User patientUser1 = createUserIfNotExists(
                "patient1@medvault.com",
                "Patient One",
                "patient123",
                patientRole
        );

        User patientUser2 = createUserIfNotExists(
                "patient2@medvault.com",
                "Patient Two",
                "patient111",
                patientRole
        );

        // ---- 3. Ensure Doctor–Patient Links Exist ----
        createDoctorPatientLink(doctorUser, patientUser1);
        createDoctorPatientLink(doctorUser, patientUser2);

        System.out.println("✅ Bootstrap completed successfully!");
    }

    // ------------------- Helper Methods -------------------

    private User createUserIfNotExists(String email, String name, String rawPassword, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = User.builder()
                    .email(email)
                    .name(name)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .enabled(true)
                    .build();

            // Attach role safely
            user.getRoles().add(role);
            User saved = userRepository.save(user);
            System.out.println("🆕 Created user: " + email + " with role: " + role.getName());
            return saved;
        });
    }

    private void createDoctorPatientLink(User doctor, User patient) {
        if (doctor == null || patient == null) {
            System.out.println("⚠️ Skipping invalid doctor-patient mapping (one is null)");
            return;
        }

        if (!doctorPatientRepository.existsByDoctorAndPatient(doctor, patient)) {
            doctorPatientRepository.save(
                    DoctorPatient.builder()
                            .doctor(doctor)
                            .patient(patient)
                            .build()
            );
            System.out.println("🔗 Linked doctor " + doctor.getEmail() + " ↔ patient " + patient.getEmail());
        }
    }
}
