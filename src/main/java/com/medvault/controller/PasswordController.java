package com.medvault.controller;

import com.medvault.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    // show forgot password page
    @GetMapping("/forgot-password")
    public String showForgotPage(){
        return "forgot_password"; // Renders forgot_password.html
    }

    // process forgot password form
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model){
        try{
            passwordService.sendResetLink(email);
            model.addAttribute("message", "✅ Password reset link sent! Please check your email.");
        }catch (Exception e){
            model.addAttribute("message",  "⚠️ " + e.getMessage());
        }
        return "forgot_password";
    }

    // show reset password page(When user clicks link)
    @GetMapping("/reset-password")
    public String showResetPage(@RequestParam String token, Model model){
        model.addAttribute("token", token);
        return "reset_password"; // Renders reset_password.html
    }

    // process reset password form submission
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
                                       @RequestParam String newPassword,
                                       Model model){
        boolean success = passwordService.resetPassword(token,newPassword);

        if(success){
            model.addAttribute("message", "✅ Password successfully updated! You can now log in.");
        }else {
            model.addAttribute("message","⚠ Invalid or expired reset link. Please request again.");
        }
        return "reset_password";
    }
}
