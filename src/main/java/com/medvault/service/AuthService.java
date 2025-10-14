package com.medvault.service;

import com.medvault.model.User;
import com.medvault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    /** Returns the currently logged-in User entity from the database */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        // the username/email is stored in the authentication object
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
