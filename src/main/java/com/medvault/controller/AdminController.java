package com.medvault.controller;

import com.medvault.model.AuditLog;
import com.medvault.model.User;
import com.medvault.repository.AuditLogRepository;
import com.medvault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AuditLogRepository auditLogRepo;

    // admin dashboard: shows all users and their logs regarding patients and Doctor
    @GetMapping("/dashboard")
    public String dashboard(Model model){
        List<User> users = userRepo.findAll();
        List<AuditLog> logs = auditLogRepo.findAll();
        model.addAttribute("users",users);
        model.addAttribute("logs", logs);
        return "dashboards/admin";
    }

    // Export logs as CSV Files
    @GetMapping("/logs/export")
    public void exportLogs(HttpServletResponse response) throws IOException{
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename = audit_logs.csv");

        try(java.io.PrintWriter writer = response.getWriter()){
            writer.println("User,Action,Timestamp");
            for (AuditLog log : auditLogRepo.findAll()) {
                writer.println(
                        log.getActor().getEmail() + "," + // who performed it
                                log.getRecord().getOriginalFilename() + "," +    // which file
                                log.getAction() + "," +  // upload/download
                                log.getAt() // when
                );
            }
        }

    }

    @GetMapping("/user/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id){
        User user = userRepo.findById(id).orElseThrow();
        user.setEnabled(false);
        userRepo.save(user);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/user/{id}/reactivate")
    public String reactivateUser(@PathVariable Long id){
        User user = userRepo.findById(id).orElseThrow();
        user.setEnabled(true);
        userRepo.save(user);
        return "redirect:/admin/dashboard";
    }
    @GetMapping("/user/{id}/delete")
    public String deleteUser(@PathVariable Long id){
        userRepo.deleteById(id);
        return "redirect:/admin/dashboard";
    }
}
