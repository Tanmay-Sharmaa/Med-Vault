package com.medvault.controller;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // display login page
    @GetMapping("/post-login")
    public String postLogin(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        if(role.equals("ROLE_ADMIN")){
            return "redirect:/admin/dashboard";
        }
        else if (role.equals("ROLE_DOCTOR")) {
            return "redirect:/doctor/dashboard";
        } else  {
            return "redirect:/patient/dashboard";
        }
    }
}
