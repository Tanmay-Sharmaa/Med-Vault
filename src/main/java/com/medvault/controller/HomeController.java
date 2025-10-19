package com.medvault.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String homeRedirect(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Redirect logged-in users to their dashboard
            if (authentication.getAuthorities().toString().contains("ROLE_ADMIN")) {
                return "redirect:/admin/dashboard";
            } else if (authentication.getAuthorities().toString().contains("ROLE_DOCTOR")) {
                return "redirect:/doctor/dashboard";
            } else {
                return "redirect:/patient/dashboard";
            }
        }
        // Default: not logged in → go to signup
        return "redirect:/signup";
    }
}
