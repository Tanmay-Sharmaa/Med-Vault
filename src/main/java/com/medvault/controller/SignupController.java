package com.medvault.controller;


import com.medvault.model.User;
import org.springframework.ui.Model;
import com.medvault.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SignupController {
    private final UserService userService;


    @GetMapping("/signup")
    public String showSignupForm(Model model){
        System.out.println("✅ /signup endpoint reached!");
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@ModelAttribute("user") User user, Model model){
        userService.registerUser(user);// remember this might give error
        model.addAttribute("message", "Verification email sent! Please check your inbox. ");
        return "login";
    }

    @GetMapping("/verify")
    public String verifyAccount(@RequestParam("token")String token, Model model){
        boolean verified = userService.verifyUser(token);
        if (verified){
            model.addAttribute("message", "Account verified Successfully! You can now log in.");
            return "verify-success";
        }
        else{
            model.addAttribute("message", "Invalid or Expired token");
            return "error";
        }
    }
}
