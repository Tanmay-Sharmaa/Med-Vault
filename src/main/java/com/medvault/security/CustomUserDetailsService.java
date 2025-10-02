package com.medvault.security;

import com.medvault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Login attempt for: " + email);
        var u = userRepo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Not found"));
        var authorities = u.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getName()))
                .toList();
        System.out.println("Loaded user: " + u.getEmail() + " / " + u.getPasswordHash());


        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .authorities(authorities)
                .disabled(!u.isEnabled())
                .build();
    }

}
