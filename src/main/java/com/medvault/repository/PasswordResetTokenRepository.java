package com.medvault.repository;

import com.medvault.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    // Find token record by its token string (used during password reset)
    Optional<PasswordResetToken> findByToken(String token);
}
