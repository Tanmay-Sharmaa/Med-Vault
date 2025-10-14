package com.medvault.controller;


import com.medvault.model.AuditLog;
import com.medvault.model.DoctorNote;
import com.medvault.model.MedicalRecord;
import com.medvault.model.User;
import com.medvault.repository.AuditLogRepository;
import com.medvault.repository.DoctorNoteRepository;
import com.medvault.repository.MedicalRecordRepository;
import com.medvault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientDashBoardController {
    @Autowired
    private MedicalRecordRepository recordRepo;
    @Autowired
    private AuditLogRepository auditRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private DoctorNoteRepository doctorNoteRepository;

    @GetMapping("/dashboard")
    public String patientDashboard(Model model){
        Authentication  auth = SecurityContextHolder.getContext().getAuthentication();
        String patientEmail = auth.getName();

        // Get the current patient from DB
        User patient = userRepo.findByEmail(patientEmail).orElseThrow();

        // Fetch all records for this patient
        List<MedicalRecord> records = recordRepo.findByPatientEmail(patientEmail);

        // Fetch audit logs for this patient
        List<AuditLog> logs = auditRepo.findAllByRecord_Patient_EmailOrderByAtDesc(patientEmail);

        // Fetch doctor notes for this patient
        List<DoctorNote> notes = doctorNoteRepository.findByPatientId(patient.getId());

        model.addAttribute("records",records);
        model.addAttribute("logs",logs);
        model.addAttribute("notes", notes);

        return "dashboards/patient";

    }


}
