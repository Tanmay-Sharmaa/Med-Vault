package com.medvault.service;


import com.medvault.model.Role;
import com.medvault.repository.RoleRepository;
import com.medvault.model.User;
import com.medvault.model.VerificationToken;
import com.medvault.repository.UserRepository;
import com.medvault.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final VerificationTokenRepository tokenRepo;
    private final RoleRepository roleRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(User user) {
        user.setEnabled(false);

        // Hash the actual password from signup form
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        // Find and assign selected role
        String fullRoleName = user.getSelectedRole().startsWith("ROLE_")
                ? user.getSelectedRole()
                : "ROLE_" + user.getSelectedRole();

        Role selectedRole = roleRepo.findByName(fullRoleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + fullRoleName));

        user.getRoles().add(selectedRole);

        userRepo.save(user);

        // Create verification token
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        tokenRepo.save(verificationToken);

        // Send email
        String verifyLink = "http://localhost:8080/verify?token=" + token;
        String body = "Dear " + user.getName() + ",\n\n"
                + "Please click the link below to verify your MedVault account:\n"
                + verifyLink + "\n\nThis link will expire in 15 minutes.\n\n- MedVault Team";

        emailService.sendVerificationEmail(user.getEmail(), body);
    }

    public boolean verifyUser(String token){
        VerificationToken vt = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token!"));

        if(vt.isExpired()){
            return false;
        }

        User user = vt.getUser();
        user.setEnabled(true);
        userRepo.save(user);
        tokenRepo.delete(vt);
        return true;
    }
}
