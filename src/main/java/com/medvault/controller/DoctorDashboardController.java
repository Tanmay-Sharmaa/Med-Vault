package com.medvault.controller;

import com.medvault.model.DoctorPatient;
import com.medvault.repository.DoctorPatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorDashboardController {
    @Autowired
    private DoctorPatientRepository doctorPatientRepository;

    @GetMapping("/dashboard")
    public String doctorDashboard(Model model){
        // Get logged in doctor's email
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String doctorEmail = auth.getName();

        // fetch patient assigned to this doctor
        List<DoctorPatient> assignedPatients = doctorPatientRepository.findByDoctorEmail(doctorEmail);

        // pass them to the HTML page
        model.addAttribute("patients", assignedPatients);

        return "dashboards/doctor";
    }
}
