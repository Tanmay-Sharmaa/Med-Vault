package com.medvault.service;

import com.medvault.model.User;
import com.medvault.model.DoctorPatient;
import com.medvault.repository.UserRepository;
import com.medvault.repository.DoctorPatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final UserRepository userRepository;
    private final DoctorPatientRepository doctorPatientRepository;

    // Get all doctors
    public List<User> getAllDoctors() {
        return userRepository.findByRoleName("ROLE_DOCTOR");
    }

    // Get all patients
    public List<User> getAllPatients() {
        return userRepository.findByRoleName("ROLE_PATIENT");
    }

    // Assign doctor to patient using email
    public void assignDoctorToPatient(String doctorEmail, String patientEmail) {
        // Find doctor and patient by email
        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("❌ Doctor not found with email: " + doctorEmail));

        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("❌ Patient not found with email: " + patientEmail));

        // Check if already assigned
        if (doctorPatientRepository.existsByDoctorAndPatient(doctor, patient)) {
            throw new RuntimeException("⚠️ This patient is already assigned to the selected doctor!");
        }

        // Save new doctor–patient mapping
        DoctorPatient doctorPatient = DoctorPatient.builder()
                .doctor(doctor)
                .patient(patient)
                .build();

        doctorPatientRepository.save(doctorPatient);
    }
    public List<DoctorPatient> getAllAssignments() {
        return doctorPatientRepository.findAll();
    }
    public void unassignDoctorFromPatient(Long mappingId) {
        doctorPatientRepository.deleteById(mappingId);
    }


}
