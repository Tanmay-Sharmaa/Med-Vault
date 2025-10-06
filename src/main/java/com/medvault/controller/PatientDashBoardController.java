package com.medvault.controller;


import com.medvault.model.AuditLog;
import com.medvault.model.MedicalRecord;
import com.medvault.repository.AuditLogRepository;
import com.medvault.repository.MedicalRecordRepository;
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

    @GetMapping("/dashboard")
    public String patientDashboard(Model model){
        Authentication  auth = SecurityContextHolder.getContext().getAuthentication();
        String patientEmail = auth.getName();

        // Fetch all records for this patient
        List<MedicalRecord> records = recordRepo.findByPatientEmail(patientEmail);

        // Fetch audit logs for this patient
        List<AuditLog> logs = auditRepo.findAllByRecord_Patient_EmailOrderByAtDesc(patientEmail);


        model.addAttribute("records",records);
        model.addAttribute("logs",logs);

        return "dashboards/patient";

    }


}
