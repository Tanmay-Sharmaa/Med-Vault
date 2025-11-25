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

import java.util.Collections;

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

        // ----  Ensure Roles Exist ----
        Role adminRole = getOrCreateRole("ROLE_ADMIN");
        Role doctorRole = getOrCreateRole("ROLE_DOCTOR");
        Role patientRole = getOrCreateRole("ROLE_PATIENT");

        // ----  Ensure Core Users Exist ----
        User adminUser = getOrCreateUser("admin@medvault.com", "Admin", "admin123", adminRole);
        User doctorUser = getOrCreateUser("doctor1@medvault.com", "Doctor One", "doctor123", doctorRole);
        User patientUser1 = getOrCreateUser("patient1@medvault.com", "Patient One", "patient123", patientRole);
        User patientUser2 = getOrCreateUser("patient2@medvault.com", "Patient Two", "patient111", patientRole);

        // ----  Clean Up Broken References (Safety Net) ----
        cleanUpOrphanDoctorPatientLinks();

        // ----  Ensure Doctor–Patient Assignments Exist ----
        createDoctorPatientLink(doctorUser, patientUser1);
        createDoctorPatientLink(doctorUser, patientUser2);

        System.out.println("✅ Bootstrap completed successfully!");
    }

    // ------------------- Helper Methods -------------------

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role saved = roleRepository.save(new Role(null, roleName));
                    System.out.println("🆕 Created role: " + roleName);
                    return saved;
                });
    }

    private User getOrCreateUser(String email, String name, String password, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setEnabled(true);
            user.setRoles(Collections.singleton(role)); // ensures roles is never null
            User saved = userRepository.save(user);
            System.out.println("🆕 Created user: " + email + " with role: " + role.getName());
            return saved;
        });
    }

    private void createDoctorPatientLink(User doctor, User patient) {
        if (doctor == null || patient == null) {
            System.out.println("⚠️ Skipping doctor-patient link (null detected)");
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

    /**
     * Cleans up orphaned doctor-patient records if related users are deleted manually.
     * This prevents SQLIntegrityConstraintViolationException on future inserts.
     */
    private void cleanUpOrphanDoctorPatientLinks() {
        doctorPatientRepository.findAll().forEach(link -> {
            if (link.getDoctor() == null || link.getPatient() == null) {
                doctorPatientRepository.delete(link);
                System.out.println("🧹 Removed orphaned doctor-patient link id=" + link.getId());
            }
        });
    }
}
