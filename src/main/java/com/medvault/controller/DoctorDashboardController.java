package com.medvault.controller;

import com.medvault.model.DoctorPatient;
import com.medvault.repository.DoctorPatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.medvault.model.DoctorNote;
import com.medvault.model.User;
import com.medvault.repository.DoctorNoteRepository;
import com.medvault.repository.UserRepository;
import com.medvault.service.AuthService;
import java.time.Instant;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorDashboardController {
    private final DoctorPatientRepository doctorPatientRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final DoctorNoteRepository doctorNoteRepository;


    @GetMapping("/dashboard")
    public String doctorDashboard(Model model) {
        // Get logged in doctor's email
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String doctorEmail = auth.getName();

        // fetch patient assigned to this doctor
        List<DoctorPatient> assignedPatients = doctorPatientRepository.findByDoctorEmail(doctorEmail);

        // pass them to the HTML page
        model.addAttribute("patients", assignedPatients);

        return "dashboards/doctor";
    }

    @PostMapping("/{patientId}/notes")
    public String addNote(@PathVariable Long patientId, @RequestParam String note) {
        User doctor = authService.getCurrentUser();               // current logged-in doctor
        User patient = userRepository.findById(patientId).orElseThrow();
        DoctorNote doctorNote = DoctorNote.builder()
                .doctor(doctor)
                .patient(patient)
                .note(note)
                .createdAt(Instant.now())
                .build();
        doctorNoteRepository.save(doctorNote);

        return "redirect:/doctor/dashboard?addedNote";

    }
}
