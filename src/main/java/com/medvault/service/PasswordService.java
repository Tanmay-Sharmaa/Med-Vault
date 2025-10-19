package com.medvault.service;


import com.medvault.model.PasswordResetToken;
import com.medvault.model.User;
import com.medvault.repository.PasswordResetTokenRepository;
import com.medvault.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // step 1 : Generate token and reset link to the user

    public void sendResetLink(String email){
        // check if user exists with the given email
        User user = userRepo.findByEmail(email).orElseThrow(()-> new RuntimeException("No account found with this email!!"));

        // generate random unique token
        String token = UUID.randomUUID().toString();

        // Build token entity( expires in 15 mins)
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        //save in Database
        tokenRepo.save(resetToken);

        // Send reset token via email
        emailService.sendPasswordResetEmail(email,token);

    }

    // verify the token and then reset the password

    public boolean resetPassword(String token, String newPassword){
        // find the token record in DB
        PasswordResetToken  prt = tokenRepo.findByToken(token)
                .orElseThrow(()->new RuntimeException("Invalid or expired reset link"));

        // Check expiry
        if(prt.isExpired()){
            tokenRepo.delete(prt); // cleanup expired token
            return false;
        }

        // update user password
        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        // Delete token so it can not be re-used
        tokenRepo.delete(prt);
        return true;
    }


}
