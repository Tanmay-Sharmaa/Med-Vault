package com.medvault.controller;

import com.medvault.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping("/assign")
    public String showAssignmentPage(Model model) {
        model.addAttribute("doctors", assignmentService.getAllDoctors());
        model.addAttribute("patients", assignmentService.getAllPatients());
        model.addAttribute("assignments", assignmentService.getAllAssignments());
        return "assign";
    }

    @PostMapping("/assign")
    public String assignDoctorToPatient(@RequestParam String doctorEmail,
                                        @RequestParam String patientEmail,
                                        Model model) {
        try {
            assignmentService.assignDoctorToPatient(doctorEmail, patientEmail);
            model.addAttribute("success", "✅ Patient successfully assigned to doctor!");
            model.addAttribute("doctors", assignmentService.getAllDoctors());
            model.addAttribute("patients", assignmentService.getAllPatients());
            model.addAttribute("assignments", assignmentService.getAllAssignments());
            return "assign";
        } catch (Exception e) {
            System.err.println("❌ Assignment error: " + e.getMessage());
            return "admin-error"; // redirect to new admin error page
        }
    }
    @PostMapping("/unassign/{id}")
    public String unassignDoctorFromPatient(@PathVariable("id") Long mappingId, Model model) {
        try {
            assignmentService.unassignDoctorFromPatient(mappingId);
            model.addAttribute("success", "🗑 Assignment removed successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "❌ Failed to remove assignment: " + e.getMessage());
        }

        // reload page data
        model.addAttribute("doctors", assignmentService.getAllDoctors());
        model.addAttribute("patients", assignmentService.getAllPatients());
        model.addAttribute("assignments", assignmentService.getAllAssignments());
        return "assign";
    }


}
