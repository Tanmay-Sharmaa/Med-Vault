package com.medvault.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.medvault.security.CustomUserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService uds; // your CustomUserDetailService

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider(UserDetailsService uds, PasswordEncoder encoder) {
        var p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(encoder);
        return p;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DaoAuthenticationProvider authProvider) throws Exception {
        http
                .authenticationProvider(authProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/signup",          //  allow signup page
                                "/verify",          //  allow verification link
                                "/forgot-password", //  future use
                                "/reset-password",  //  future use
                                "/test-email",
                                "/error",
                                "/css/**",
                                "/js/**"
                        ).permitAll()
                        .requestMatchers("/records/**").hasAnyRole("DOCTOR", "PATIENT", "ADMIN") // ✅ secure all record endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/doctor/**").hasRole("DOCTOR")
                        .requestMatchers("/patient/**").hasRole("PATIENT")
                        .anyRequest().authenticated()
                )

                .formLogin(f -> f
                        .loginPage("/login").permitAll()
                        .usernameParameter("email")   // or "email" if your input name is email
                        .passwordParameter("password")
                        .failureUrl("/login?error")
                        .defaultSuccessUrl("/post-login", true)

                )
                .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll());

        return http.build();
    }


    @Bean
    AuthenticationSuccessHandler successHandler() {
        return (req, res, auth) -> {
            var roles = auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
            if (roles.contains("ROLE_ADMIN")) res.sendRedirect("/admin/dashboard");
            else if (roles.contains("ROLE_DOCTOR")) res.sendRedirect("/doctor/dashboard");
            else res.sendRedirect("/patient/dashboard");
        };
    }
}


