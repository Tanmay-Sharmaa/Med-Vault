package com.medvault.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostLoginController {

    @GetMapping("/post-login")
    public String postLogin(Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        if (role.equals("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard?welcome";
        } else if (role.equals("ROLE_DOCTOR")) {
            return "redirect:/doctor/dashboard?welcome";
        } else {
            return "redirect:/patient/dashboard?welcome";
        }
    }
}

